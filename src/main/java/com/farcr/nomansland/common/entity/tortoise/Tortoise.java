package com.farcr.nomansland.common.entity.tortoise;

import com.farcr.nomansland.common.entity.tortoise.ai.*;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.google.common.base.Suppliers;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class Tortoise extends Animal {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityDataAccessor<Optional<BlockPos>> HOME_POS = SynchedEntityData.defineId(Tortoise.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> HAS_EGG = SynchedEntityData.defineId(Tortoise.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LAYING_EGG = SynchedEntityData.defineId(Tortoise.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> GOING_HOME = SynchedEntityData.defineId(Tortoise.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IN_SHELL = SynchedEntityData.defineId(Tortoise.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SEARCHING = SynchedEntityData.defineId(Tortoise.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> HURT_WHEN = SynchedEntityData.defineId(Tortoise.class, EntityDataSerializers.LONG);
    @Nullable
    private UUID lastHurtByUUID;
    private static final float BABY_SCALE = 0.3F;
    private static final Supplier<EntityDimensions> BABY_DIMENSIONS = Suppliers.memoize(() -> NMLEntities.TORTOISE.get().getDimensions()
            .withAttachments(EntityAttachments.builder()
                    .attach(EntityAttachment.PASSENGER, 0.0F, NMLEntities.TORTOISE.get().getHeight(), -0.25F))
            .scale(BABY_SCALE));
    private int layEggCounter;
    private int timesFedWhenBaby;
    public final AnimationState hidingAnimationState = new AnimationState();
    public final AnimationState emergingAnimationState = new AnimationState();
    public final AnimationState layingEggAnimationState = new AnimationState();

    public Tortoise(EntityType<? extends Tortoise> entityType, Level level) {
        super(entityType, level);
        this.lookControl = new LookControl(this) {
            @Override
            public void tick() {
                if (Tortoise.this.inShell() && !Tortoise.this.isSearching())
                    return;
                super.tick();
            }
        };
        this.navigation = new GroundPathNavigation(this, level) {
            @Override
            public boolean isDone() {
                return super.isDone() || Tortoise.this.inShell();
            }
        };
    }

    public static boolean checkTortoiseSpawnRules(EntityType<Tortoise> tortoise, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return isBrightEnoughToSpawn(level, pos);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TortoiseSearchForDangerGoal(this));
        this.goalSelector.addGoal(1, new TortoiseStayAroundHomeGoal(this, 0.75F));
        this.goalSelector.addGoal(2, new TortoiseSleepAndWakeUpGoal(this));
        this.goalSelector.addGoal(3, new TortoiseBreedGoal(this, 0.5F));
        this.goalSelector.addGoal(4, new TortoiseLayEggGoal(this));
        this.goalSelector.addGoal(4, new TortoiseFindSpotToLayEgg(this, 0.85F));
        this.goalSelector.addGoal(4, new TemptGoal(this, 0.5F, itemStack -> itemStack.is(NMLTags.TORTOISE_FOOD), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 0.5));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.5F));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.STEP_HEIGHT, 1.0F)
                .add(Attributes.ARMOR, 15.0F);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(Tortoise.HOME_POS, Optional.empty());
        builder.define(Tortoise.HAS_EGG, false);
        builder.define(Tortoise.LAYING_EGG, false);
        builder.define(Tortoise.GOING_HOME, false);
        builder.define(Tortoise.IN_SHELL, false);
        builder.define(Tortoise.SEARCHING, false);
        builder.define(Tortoise.HURT_WHEN, 0L);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (key.equals(LAYING_EGG)) {
            resetAnimations();
            if (isLayingEgg()) layingEggAnimationState.startIfStopped(tickCount);
            refreshDimensions();
        }

        if (key.equals(IN_SHELL)) {
            resetAnimations();
            if (inShell()) hidingAnimationState.startIfStopped(tickCount);
            else emergingAnimationState.startIfStopped(tickCount);
            refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
    }

    @Override
    public void tick() {
        super.tick();
        long gameTime = this.level().getGameTime();
        if ((gameTime - this.getHurtWhen() > 600L) && this.inShell() && this.getLastHurtByUUID() != null) {
            this.setSearching(true);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (this.getHomePos() != null)
            BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, this.getHomePos()).resultOrPartial(LOGGER::error)
                    .ifPresent(tag -> compound.put("home_pos", tag));
        compound.putBoolean("HasEgg", this.hasEgg());
        compound.putBoolean("Searching", this.isSearching());
        compound.putBoolean("InShell", this.inShell());
        compound.putLong("HurtWhen", this.getHurtWhen());
        compound.putLong("EggCount", this.getLayEggCounter());
        compound.putInt("TimesFed", this.getTimesFedWhenBaby());
        if (this.lastHurtByUUID != null)
            compound.putUUID("HurtByUUID", this.lastHurtByUUID);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        BlockPos.CODEC.decode(NbtOps.INSTANCE, compound.get("home_pos")).resultOrPartial(LOGGER::error)
                .ifPresent(pair -> this.setHomePos(pair.getFirst()));
        this.setSearching(compound.getBoolean("Searching"));
        this.retreatShell(compound.getBoolean("InShell"));
        this.setHasEgg(compound.getBoolean("HasEgg"));
        this.setHurtWhen(compound.getInt("HurtWhen"));
        this.setLayEggCounter(compound.getInt("EggCount"));
        if (this.lastHurtByUUID != null)
            this.lastHurtByUUID = compound.getUUID("HurtByUUID");
        this.setTimesFedWhenBaby(compound.getInt("TimesFed"));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnType == MobSpawnType.STRUCTURE)
            this.setHomePos(this.blockPosition());
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (this.isFood(itemstack)) {
            int i = this.getAge();
            if (!this.level().isClientSide && i == 0 && this.canFallInLove()) {
                this.usePlayerItem(player, hand, itemstack);
                this.setInLove(player);
                return InteractionResult.SUCCESS;
            }

            if (this.isBaby() && this.getTimesFedWhenBaby() < 3 && !this.level().isClientSide) {
                for (int particleCount = 0; particleCount < 5; particleCount++) {
                    ((ServerLevel) level()).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemstack), this.getX() + this.getLookAngle().x / 2.0,
                            this.getY() + this.getLookAngle().y,
                            this.getZ() + this.getLookAngle().z / 2, 1, 0, 0, 0, 0.15);
                }
                this.usePlayerItem(player, hand, itemstack);
                this.playSound(
                        SoundEvents.PLAYER_BURP,
                        0.5F + 0.5F * (float) this.random.nextInt(2),
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F
                );
                this.ageUp(1800);
                this.timesFedWhenBaby++;
                return InteractionResult.SUCCESS;
            }

            if (this.level().isClientSide) {
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.PASS;
    }

    private void spawnItemParticles(ItemStack stack, int amount) {
    }

    /**
     * Set whether this mob is a child.
     *
     * @param baby
     */
    @Override
    public void setBaby(boolean baby) {
        this.setAge(baby ? -432000 : 0);
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return SoundEvents.TURTLE_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.TURTLE_SWIM;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return this.isBaby() ? SoundEvents.TURTLE_HURT_BABY : SoundEvents.TURTLE_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return this.isBaby() ? SoundEvents.TURTLE_DEATH_BABY : SoundEvents.TURTLE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        super.playStepSound(pos, block);
    }

    @Override
    public boolean canFallInLove() {
        return super.canFallInLove() && !this.hasEgg() && !this.inShell();
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.15F;
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? BABY_SCALE : 1.0F;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return NMLEntities.TORTOISE.get().create(level);
    }

    @Override
    protected float getKnockback(Entity attacker, DamageSource damageSource) {
        return this.inShell() ? 0.0F : super.getKnockback(attacker, damageSource);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(NMLTags.TORTOISE_FOOD);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity) {
            if (!this.inShell()) {
                this.setLastHurtByMob((LivingEntity) source.getEntity());
                this.setHurtWhen(this.level().getGameTime());
                this.retreatShell(true);
            } else {
                if (amount > this.getMaxHealth()) {
                    amount -= this.getMaxHealth();
                    this.playSound(this.getHurtSound(source));
                } else {
                    this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.2F);
                    return false;
                }
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void setLastHurtByMob(@Nullable LivingEntity livingEntity) {
        super.setLastHurtByMob(livingEntity);
        if (livingEntity != null)
            this.setLastHurtByUUID(livingEntity.getUUID());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isAlive() && this.isLayingEgg() && this.layEggCounter >= 1 && this.layEggCounter % 5 == 0) {
            BlockPos blockpos = this.blockPosition();
            BlockState blockstate = this.level().getBlockState(blockpos.below());
            if (blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                for (int i = 0; i < 5; i++) {
                    ((ServerLevel) this.level())
                            .sendParticles(
                                    new BlockParticleOption(ParticleTypes.BLOCK, blockstate),
                                    (double) blockpos.getX() + 0.5,
                                    (double) blockpos.getY() + 0.7,
                                    (double) blockpos.getZ() + 0.5,
                                    3,
                                    ((double) this.getRandom().nextFloat() - 0.5) * 0.08,
                                    ((double) this.getRandom().nextFloat() - 0.5) * 0.08,
                                            ((double) this.getRandom().nextFloat() - 0.5) * 0.08,
                                    0.15F
                            );
                }
                this.playSound(blockstate.getSoundType(level(), blockpos.below(), this).getPlaceSound());
            }
            this.gameEvent(GameEvent.ENTITY_ACTION);
        }
    }


    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        if (!this.isBaby() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.spawnAtLocation(new ItemStack(NMLItems.STURDY_SCUTE.get(), this.random.nextIntBetweenInclusive(1, 2)), 1);
        }
    }

    private void resetAnimations() {
        hidingAnimationState.stop();
        emergingAnimationState.stop();
        layingEggAnimationState.stop();
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return 0.0F;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS.get() : super.getDefaultDimensions(pose);
    }

    public void setHomePos(BlockPos homePos) {
        this.entityData.set(HOME_POS, Optional.ofNullable(homePos));
    }

    public BlockPos getHomePos() {
        return this.entityData.get(HOME_POS).orElse(null);
    }

    public boolean hasEgg() {
        return this.entityData.get(HAS_EGG);
    }

    public void setHasEgg(boolean hasEgg) {
        this.entityData.set(HAS_EGG, hasEgg);
    }

    public boolean isLayingEgg() {
        return this.entityData.get(LAYING_EGG);
    }

    public boolean inShell() {
        return this.entityData.get(IN_SHELL);
    }

    public void retreatShell(boolean shouldShell) {
        this.entityData.set(IN_SHELL, shouldShell);
        this.stopInPlace();
    }

    public boolean isSearching() {
        return this.entityData.get(SEARCHING);
    }

    public void setSearching(boolean searching) {
        this.entityData.set(SEARCHING, searching);
    }

    public void setLayingEgg(boolean isLayingEgg) {
        this.layEggCounter = isLayingEgg ? 1 : 0;
        this.entityData.set(LAYING_EGG, isLayingEgg);
    }

    public boolean isGoingHome() {
        return this.entityData.get(GOING_HOME);
    }

    public void setGoingHome(boolean isGoingHome) {
        this.entityData.set(GOING_HOME, isGoingHome);
    }

    public long getHurtWhen() {
        return this.entityData.get(HURT_WHEN);
    }

    public void setHurtWhen(long hurtWhen) {
        this.entityData.set(HURT_WHEN, hurtWhen);
    }

    @Nullable
    public UUID getLastHurtByUUID() {
        return lastHurtByUUID;
    }

    public void setLastHurtByUUID(@Nullable UUID lastHurtByUUID) {
        this.lastHurtByUUID = lastHurtByUUID;
    }

    public int getLayEggCounter() {
        return layEggCounter;
    }

    public void setLayEggCounter(int layEggCounter) {
        this.layEggCounter = layEggCounter;
    }

    /**
     * a Home, to Tortoises, is a location that has 0 Sky light, No direct sky access,
     * a light level lower than 7, and above a block within the correct tag (#suitable_turtle_home).
     */
    public boolean isValidHome(BlockPos pos) {
        return level().getBlockState(pos.below()).is(NMLTags.SUITABLE_TORTOISE_HOME) && (level().getBrightness(LightLayer.BLOCK, pos) < 7 && level().getBrightness(LightLayer.SKY, pos) < 7) && !level().canSeeSky(pos) && !level().getBlockState(pos).is(NMLBlocks.TORTOISE_EGGS);
    }

    public int getTimesFedWhenBaby() {
        return timesFedWhenBaby;
    }

    public void setTimesFedWhenBaby(int timesFedWhenBaby) {
        this.timesFedWhenBaby = timesFedWhenBaby;
    }
}