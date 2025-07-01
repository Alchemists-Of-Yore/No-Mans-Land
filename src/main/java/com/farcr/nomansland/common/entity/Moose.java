package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.entities.NMLEntityDataSerializers;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.IntFunction;

public class Moose extends Animal /*implements NeutralMob*/ {
    //private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Moose.class, EntityDataSerializers.INT);
    //private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(2*20*60, 2*20*60); // figure out how to make this dynamically take day length?
    private static final EntityDataAccessor<Integer> DATA_PACIFICATION_STAGE = SynchedEntityData.defineId(Moose.class, EntityDataSerializers.INT); // number of carrots fed, or 5 if pacified
    private static final EntityDataAccessor<MooseState> MOOSE_STATE = SynchedEntityData.defineId(Moose.class, NMLEntityDataSerializers.MOOSE_STATE.get());
    @Nullable
    private UUID persistentAngerTarget;
    private long inStateTicks = 0L;
    public final AnimationState stompAnimationState = new AnimationState();

    public Moose(EntityType<? extends Animal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 40D)
                .add(Attributes.FOLLOW_RANGE, 20D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.1f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5f)
                .add(Attributes.ATTACK_DAMAGE, 2f);
    }

    public MooseState getState() {
        return this.entityData.get(MOOSE_STATE);
    }

    public void switchToState(MooseState state) {
        this.entityData.set(MOOSE_STATE, state);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (MOOSE_STATE.equals(key)) {
            this.inStateTicks = 0L;
        }

        super.onSyncedDataUpdated(key);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource randomSource = level.getRandom();
        MooseAI.initMemories(this, randomSource);

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MOOSE_STATE, MooseState.IDLE);
        //builder.define(DATA_REMAINING_ANGER_TIME, 0);
        builder.define(DATA_PACIFICATION_STAGE, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        //this.addPersistentAngerSaveData(compound);
        compound.putString("state", this.getState().getSerializedName());
        compound.putInt("PacificationStage", getPacificationStage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        //this.readPersistentAngerSaveData(this.level(), compound);
        this.switchToState(MooseState.fromName(compound.getString("state")));
        if (compound.contains("PacificationStage")) {
            setPacificationStage(compound.getInt("PacificationStage"));
        }
    }

    public void beginStomp() {
        if (this.getState() == MooseState.IDLE) {
            this.stopInPlace();
            this.resetLove();
            this.gameEvent(GameEvent.ENTITY_ACTION);
            this.switchToState(MooseState.STOMPING);
        }
    }

    public void endStomp() {
        if (this.getState() == MooseState.STOMPING) {
            this.gameEvent(GameEvent.ENTITY_ACTION);
            this.switchToState(MooseState.IDLE);
        }
    }

    public boolean shouldEndStomping() {
        return this.getState() == MooseState.STOMPING && this.inStateTicks >= 1.5 * 20;
    }

    @Override
    protected Brain.Provider<Moose> brainProvider() {
        return MooseAI.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return MooseAI.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    protected void customServerAiStep() {
        this.level().getProfiler().push("mooseBrain");
        ((Brain<Moose>) this.brain).tick((ServerLevel) this.level(), this);
        this.level().getProfiler().pop();
        this.level().getProfiler().push("mooseActivityUpdate");
        MooseAI.updateActivity(this);
        this.level().getProfiler().pop();

        super.customServerAiStep();
    }

    /*@Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel) this.level(), true);
        }
    }*/

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
        if (shouldEndStomping()) {
            this.endStomp();
        }

        this.inStateTicks++;

    }

    /*@Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new HurtByTargetGoal(this));

        this.goalSelector.addGoal(2, new NearestAttackableTargetGoal<>(this,  Player.class, 10, true, false, this::isAngryAt));
        this.goalSelector.addGoal(3, new MooseChargeAttackGoal(this, 1.0, 1.5, 16, 4));

        this.goalSelector.addGoal(4, new BreedGoal(this, 1.15D));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.2D, Ingredient.of(NMLTags.MOOSE_FOOD), false));

        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.1D));


        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.1D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 3f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(7, new ResetUniversalAngerTargetGoal<>(this, false));

    }*/

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        Moose baby = NMLEntities.MOOSE.get().create(pLevel);
        if (baby != null && isPacified() && ((Moose) pOtherParent).isPacified()) {
            baby.setPacificationStage(5);
        }
        if (baby != null) {
            MooseAI.initMemories(baby, pLevel.getRandom());
        }
        return baby;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Item item = itemstack.getItem();
        if (!this.level().isClientSide || this.isBaby() && this.isFood(itemstack)) {
            if (itemstack.is(Items.GOLDEN_CARROT) && getPacificationStage() < 5) {
                int pacificationStage = getPacificationStage();
                itemstack.consume(1, player);
                if (pacificationStage < 4) {
                    setPacificationStage(pacificationStage + 1);
                    this.level().broadcastEntityEvent(this, (byte) 6);
                } else if (random.nextInt(3) == 0) {
                    setPacificationStage(5);
                    this.navigation.stop();
                    this.setTarget(null);
                    //this.setPersistentAngerTarget(null);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
                return InteractionResult.SUCCESS;
            } else {
                return super.mobInteract(player, hand);
            }
        } else {
            boolean flag = itemstack.is(Items.GOLDEN_CARROT) && getPacificationStage() < 5;
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
    }

    protected void spawnTamingParticles(boolean tamed) {
        ParticleOptions particleoptions = ParticleTypes.HEART;
        if (!tamed) {
            particleoptions = ParticleTypes.SMOKE;
        }

        for(int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02;
            double d1 = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(particleoptions, this.getRandomX((double)1.0F), this.getRandomY() + (double)0.5F, this.getRandomZ((double)1.0F), d0, d1, d2);
        }

    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 7) {
            this.spawnTamingParticles(true);
        } else if (id == 6) {
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(id);
        }
    }

    public int getPacificationStage() {
        return this.entityData.get(DATA_PACIFICATION_STAGE);
    }

    public void setPacificationStage(int stage) {
        this.entityData.set(DATA_PACIFICATION_STAGE, stage);
    }

    public boolean isPacified() {
        return getPacificationStage() == 5;
    }

    @Override
    public boolean isFood(ItemStack pStack) {
        return pStack.is(NMLTags.MOOSE_FOOD);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HOGLIN_AMBIENT;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.RAVAGER_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.DOLPHIN_DEATH;
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.75f : 1.0f;
    }

    private void setupAnimationStates() {
        switch (this.getState()) {
            case IDLE:
                this.stompAnimationState.stop();
                break;
            case STOMPING:
                this.stompAnimationState.startIfStopped(this.tickCount);
                break;
        }
    }

    /*@Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, time);
    }

    @Override
    public @Nullable UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }*/

    @Override
    public @Nullable LivingEntity getTarget() {
        if (isPacified()) return null;
        return this.getTargetFromBrain();
    }

    public static enum MooseState implements StringRepresentable {
        IDLE("idle", 0),
        STOMPING("stomping", 1),
        CHARGING("charging", 2);

        private static final StringRepresentable.EnumCodec<MooseState> CODEC = StringRepresentable.fromEnum(MooseState::values);
        private static final IntFunction<MooseState> BY_ID = ByIdMap.continuous(
                MooseState::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO
        );
        public static final StreamCodec<ByteBuf, MooseState> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, MooseState::id);
        private final String name;
        private final int id;

        MooseState(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public static MooseState fromName(String name) {return CODEC.byName(name, IDLE);}

        @Override
        public String getSerializedName() {return this.name;}

        private int id() {return this.id;}
    }
}