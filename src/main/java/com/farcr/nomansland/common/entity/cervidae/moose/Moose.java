package com.farcr.nomansland.common.entity.cervidae.moose;

import com.farcr.nomansland.common.entity.cervidae.IAntlers;
import com.farcr.nomansland.common.entity.cervidae.ShedAntlersGoal;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Moose extends PathfinderMob implements PlayerRideable, Saddleable, IAntlers {

    private static final EntityDataAccessor<Boolean> DATA_HAS_ANTLERS = SynchedEntityData.defineId(Moose.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_SADDLED = SynchedEntityData.defineId(Moose.class, EntityDataSerializers.BOOLEAN);

    public static final byte ADD_STOMP_FEEDBACK_EVENT = 11;
    public static final byte START_STOMP_EVENT = 10;
    public static final byte ATTACK_EVENT = 9;
    public static final byte TAME_EVENT = 8;
    public static final byte EAT_EVENT = 7;
    public static final byte REJECT_FOOD_EVENT = 6;

    public static final int STOMP_COOLDOWN = 100;
    public static final int STOMP_DURATION = 20;
    public static final int STOMP_EFFECT_DELAY = 15;
    public static final float ACTIVE_STOMP_SPEED_MULTIPLIER = 0.3f;
    public static final float POST_STOMP_SPEED_MULTIPLIER = 1.5f;

    public static final int SADDLE_SHAKEOFF_DELAY = 10;
    private static final int MINIMUM_TAME_ATTEMPTS = 4;
    private static final float SUCCESSFUL_TAME_CHANCE = 0.333f;

    public AnimationState stompAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();

    public int antlerTimer;
    public boolean isInStompState;
    public int stompStateTimer;
    public int saddleShakeOffTimer;

    private int pacificationStage;

    public Moose(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);

        builder.define(DATA_HAS_ANTLERS, true);
        builder.define(DATA_IS_SADDLED, false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        saveAntlerData(compound);
        compound.putBoolean("IsInStompState", isInStompState);
        compound.putInt("StompStateTimer", stompStateTimer);
        compound.putInt("SaddleShakeOffTimer", saddleShakeOffTimer);

        compound.putInt("PacificationStage", getPacificationStage());
        compound.putBoolean("IsSaddled", isSaddled());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        readAntlerData(compound);
        isInStompState = compound.getBoolean("IsInStompState");
        stompStateTimer = compound.getInt("StompStateTimer");
        saddleShakeOffTimer = compound.getInt("SaddleShakeOffTimer");

        setPacificationStage(compound.getInt("PacificationStage"));
        setIsSaddled(compound.getBoolean("IsSaddled"));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.STEP_HEIGHT, 1);
    }

    public static void registerMooseRelatedGoals(Mob otherMob) {
        if (otherMob instanceof Wolf wolf) {
            wolf.goalSelector.addGoal(3, new AvoidEntityGoal<>(wolf, Moose.class, 12.0F, 1.2, 1.2));
        }

        if (otherMob instanceof Monster monster) {
            monster.goalSelector.addGoal(0,
                    new AvoidEntityGoal<>(monster, Moose.class, Moose::shouldHostilesAvoid,12.0F, 1.1, 1.1, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test));
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(0, new PanicGoal(this, 1.75));
//        goalSelector.addGoal(2, new BreedGoal(this, 1.25));
//        goalSelector.addGoal(3, new FollowParentGoal(this, 1.5));
        goalSelector.addGoal(4, new ShedAntlersGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(5, new MooseIntrovertedBehaviorGoal(this, 1.25f, 6, 2));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 14));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }


    @Override
    public boolean hasAntlers() {
        return entityData.get(DATA_HAS_ANTLERS);
    }

    @Override
    public void setHasAntlers(boolean hasAntlers) {
        entityData.set(DATA_HAS_ANTLERS, hasAntlers);
    }

    @Override
    public int getAntlerTimer() {
        return antlerTimer;
    }

    @Override
    public void setAntlerTimer(int antlersAge) {
        this.antlerTimer = antlersAge;
    }

    @Override
    public void onShedAntlers() {
        playSound(NMLSounds.MOOSE_SHEDS_ANTLERS.get(), 0.6F, 1.0F);
    }

    @Override
    public boolean isSaddleable() {
        return true;
    }

    @Override
    public void equipSaddle(@NotNull ItemStack itemStack, @Nullable SoundSource soundSource) {
        setIsSaddled(true);
    }

    @Override
    protected void customServerAiStep() {
        if (!isBaby()) {
            regrowLostAntlers(this);
        }
        if (isInStompState) {
            stompStateTimer++;
            if (stompStateTimer == STOMP_EFFECT_DELAY) {
                playSound(NMLSounds.MOOSE_STOMPS.get(), 1.5f, 1 + random.nextFloat() * 0.3f);
                level().broadcastEntityEvent(this, Moose.ADD_STOMP_FEEDBACK_EVENT);
            }
            if (stompStateTimer > STOMP_COOLDOWN) {
                isInStompState = false;
                stompStateTimer = 0;
            }
        }
        if (isSaddled() && !isPacified()) {
            saddleShakeOffTimer++;
            if (saddleShakeOffTimer == SADDLE_SHAKEOFF_DELAY) {
                shakeOffSaddle();
                saddleShakeOffTimer = 0;
            }
        }

        super.customServerAiStep();
    }

    /**
     * All the on-spawn logic.
     * Handles antlers and their lifetime. Same as the deer
     */
    @SuppressWarnings("deprecation")
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (!isBaby() && random.nextFloat() < 0.8) {
            addAntlersUponSpawning(random);
        }

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    /**
     * Handles networked entity events, including our particle stuff
     */
    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case ADD_STOMP_FEEDBACK_EVENT -> spawnStompParticles();
            case START_STOMP_EVENT -> stompAnimationState.start(tickCount);
            case ATTACK_EVENT -> attackAnimationState.start(tickCount);
            case TAME_EVENT -> spawnTamingParticles(true);
            case EAT_EVENT -> spawnTamingParticles(false);
            case REJECT_FOOD_EVENT -> spawnRejectedFoodParticles();
            default -> super.handleEntityEvent(id);
        }
    }

    /**
     * All the logic tied to the Moose hurting anything through any means is handled here.
     */
    @Override
    public boolean doHurtTarget(Entity target) {
        float baseDamage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        float damageDealt = baseDamage > 0 ? baseDamage / 2 + random.nextInt((int) baseDamage) : baseDamage;
        var damagesource = damageSources().mobAttack(this);
        if (target.hurt(damagesource, damageDealt)) {
            double knockbackResistance = target instanceof LivingEntity living ? living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) : 0;
            target.setDeltaMovement(target.getDeltaMovement().add(0, 0.4F * Math.max(0, 1 - knockbackResistance), 0));
            if (target instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetEntityMotionPacket(player));
            }
            if (level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffects(serverLevel, target, damagesource);
            }
            return true;
        }
        return false;
    }

    /**
     * All the right-click logic of the Moose is handled here.
     * Runs once for each hand whenever the mob is right-clicked.
     */
    @SuppressWarnings("OptionalIsPresent")
    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        var heldItem = player.getItemInHand(hand);

        var mountInteraction = tryMount(player);
        if (mountInteraction.isPresent()) {
            return mountInteraction.get();
        }
        var tameInteraction = tryTame(player, heldItem);
        if (tameInteraction.isPresent()) {
            return tameInteraction.get();
        }

        return super.mobInteract(player, hand);
    }
    /**
     * Controls whether hostile mobs should avoid the Moose.
     * @param theMojangMethodDoesntApplyAGenericTypeToThisThing Unless something has gone horribly wrong, this will be the Moose.
     * @return Whether the moose should be avoided at all cost.
     */
    public static boolean shouldHostilesAvoid(LivingEntity theMojangMethodDoesntApplyAGenericTypeToThisThing) {
        if (theMojangMethodDoesntApplyAGenericTypeToThisThing instanceof Moose moose) {
            return !moose.shouldScareOffMonsters();
        }
        return false;
    }

    /**
     * Shakes off the equipped saddle, dropping it on the ground.
     */
    public void shakeOffSaddle() {
        setIsSaddled(false);
        ItemEntity itementity = spawnAtLocation(Items.SADDLE, 1);
        if (itementity != null) {
            float x = (random.nextFloat() - random.nextFloat()) * 0.3F;
            float y = 0.2f + random.nextFloat() * 0.05F;
            float z = (random.nextFloat() - random.nextFloat()) * 0.3F;
            itementity.setDeltaMovement(itementity.getDeltaMovement().add(x, y, z));
        }
    }

    /**
     * Enables a player to mount the Moose if it is pacified and saddled.
     * @param player The soon-to-be rider
     * @return An optional containing the result of the interaction if it was successful
     */
    public Optional<InteractionResult> tryMount(Player player) {
        if (!isPacified()) {
            return Optional.empty();
        }
        if (!isSaddled() || isVehicle()) {
            return Optional.empty();
        }
        var isClientSide = level().isClientSide;
        if (!isClientSide) {
            doPlayerRide(player);
        }
        return Optional.of(InteractionResult.sidedSuccess(isClientSide));
    }

    /**
     * Attempts to eat moose food from a player, eventually growing pacified and enabling saddling behavior.
     * @param player The soon-to-be owner of the moose
     * @param stack The offering.
     * @return An optional containing the result of the interaction if it was successful
     */
    public Optional<InteractionResult> tryTame(Player player, ItemStack stack) {
        if (isPacified()) {
            return Optional.empty();
        }
        var level = level();
        boolean isMooseFood = stack.is(NMLTags.MOOSE_FOOD);
        if (getTarget() != null || (!isMooseFood && stack.getFoodProperties(player) != null)) {
            level.broadcastEntityEvent(this, REJECT_FOOD_EVENT);
            playSound(NMLSounds.MOOSE_REJECTS_FOOD.get(), 0.2F, 1.0F);
            return Optional.of(InteractionResult.FAIL);
        }
        if (isMooseFood) {
            var isClientSide = level.isClientSide;
            if (!isClientSide) {
                stack.shrink(1);
                playSound(NMLSounds.MOOSE_EAT.get(), 0.2F, 1.0F);
                int stage = getPacificationStage();
                if (stage < MINIMUM_TAME_ATTEMPTS) {
                    stage++;
                }
                else {
                    if (random.nextFloat() < SUCCESSFUL_TAME_CHANCE) {
                        stage++;
                        navigation.stop();
                        setTarget(null);
                        brain.getMemory(MemoryModuleType.ANGRY_AT).ifPresent(target -> {
                            if (player.getUUID().equals(target))
                                brain.eraseMemory(MemoryModuleType.ANGRY_AT);
                        });
                    }
                }
                level.broadcastEntityEvent(this, stage == MINIMUM_TAME_ATTEMPTS ? TAME_EVENT : EAT_EVENT);
                setPacificationStage(stage);
            }

            return Optional.of(InteractionResult.sidedSuccess(isClientSide));
        }
        return Optional.empty();
    }

    public float getStompAdjustedMovementSpeed(float speed) {
        if (canStomp()) {
            return speed;
        }
        if (stompStateTimer <= STOMP_EFFECT_DELAY) {
            //Actively stomping
            return speed * ACTIVE_STOMP_SPEED_MULTIPLIER;
        } else {
            //Hastened speed after stomp
            return speed * POST_STOMP_SPEED_MULTIPLIER;
        }
    }

    public boolean canStomp() {
        return !isInStompState && stompStateTimer == 0;
    }

    public boolean shouldScareOffMonsters() {
        return stompStateTimer > STOMP_EFFECT_DELAY;
    }

    /**
     * Partially Matches behavior from {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     * The Horse method disables some animation flags that the Moose doesn't use, besides that the method is equal.
     */
    protected void doPlayerRide(Player player) {
        player.setYRot(getYRot());
        player.setXRot(getXRot());
        player.startRiding(this);
    }

    /**
     * Partially Matches behavior from {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     * All non-jump-mechanic related code is brought over without changes.
     */
    @Override
    protected void tickRidden(@NotNull Player player, @NotNull Vec3 travelVector) {
        super.tickRidden(player, travelVector);
        Vec2 vec2 = getRiddenRotation(player);
        setRot(vec2.y, vec2.x);
        yRotO = yBodyRot = yHeadRot = getYRot();
    }

    /**
     * Matches behavior from {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     */
    protected Vec2 getRiddenRotation(LivingEntity entity) {
        return new Vec2(entity.getXRot() * 0.5F, entity.getYRot());
    }

    /**
     * Partially matches behavior from {@link net.minecraft.world.entity.animal.horse.AbstractHorse}.
     * The Moose cannot jump and thus the appropriate code tied to jumping behavior is removed.
     */
    @Override
    protected @NotNull Vec3 getRiddenInput(Player player, @NotNull Vec3 travelVector) {
        float x = player.xxa * 0.5F;
        float z = player.zza;
        if (z <= 0.0F) {
            z *= 0.25F;
        }

        return new Vec3(x, 0, z);
    }

    /**
     * Mostly matches behavior from {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     * Added modifier through stomping.
     */
    @Override
    protected float getRiddenSpeed(@NotNull Player player) {
        return getStompAdjustedMovementSpeed((float) getAttributeValue(Attributes.MOVEMENT_SPEED));
    }

    /**
     * Copied From & Matches aptly named method in {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     */
    @Nullable
    private Vec3 getDismountLocationInDirection(Vec3 direction, LivingEntity passenger) {
        double d0 = getX() + direction.x;
        double d1 = getBoundingBox().minY;
        double d2 = getZ() + direction.z;
        var mutable = new BlockPos.MutableBlockPos();

        for (Pose pose : passenger.getDismountPoses()) {
            mutable.set(d0, d1, d2);
            double d3 = getBoundingBox().maxY + 0.75;

            while (true) {
                double d4 = level().getBlockFloorHeight(mutable);
                if ((double) mutable.getY() + d4 > d3) {
                    break;
                }

                if (DismountHelper.isBlockFloorValid(d4)) {
                    AABB aabb = passenger.getLocalBoundsForPose(pose);
                    Vec3 vec3 = new Vec3(d0, (double) mutable.getY() + d4, d2);
                    if (DismountHelper.canDismountTo(level(), passenger, aabb.move(vec3))) {
                        passenger.setPose(pose);
                        return vec3;
                    }
                }

                mutable.move(Direction.UP);
                if ((double) mutable.getY() < d3) {
                    break;
                }
            }
        }

        return null;
    }

    /**
     * Copied From & Matches {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     */
    public @NotNull Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        float mooseWidth = getBbWidth();
        float passengerWidth = livingEntity.getBbWidth();
        Vec3 collisionVector = getCollisionHorizontalEscapeVector(mooseWidth, passengerWidth, getYRot() + (livingEntity.getMainArm() == HumanoidArm.RIGHT ? 90.0F : -90.0F));
        Vec3 dismountLocation = getDismountLocationInDirection(collisionVector, livingEntity);
        if (dismountLocation != null) {
            return dismountLocation;
        } else {
            Vec3 vec32 = getCollisionHorizontalEscapeVector(mooseWidth, passengerWidth, getYRot() + (livingEntity.getMainArm() == HumanoidArm.LEFT ? 90.0F : -90.0F));
            Vec3 vec33 = getDismountLocationInDirection(vec32, livingEntity);
            return vec33 != null ? vec33 : position();
        }
    }

    /**
     * Copied From & Matches {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     */
    protected void spawnTamingParticles(boolean tamed) {
        var particle = tamed ? ParticleTypes.HEART : ParticleTypes.SMOKE;
        double motion = 0.02;
        for(int i = 0; i < 7; i++) {
            double x = random.nextGaussian() * motion;
            double y = random.nextGaussian() * motion;
            double z = random.nextGaussian() * motion;
            level().addParticle(particle, getRandomX(1.0F), getRandomY() + (double)0.5F, getRandomZ(1.0F), x, y, z);
        }
    }

    protected void spawnRejectedFoodParticles() {
        double motion = 0.04;
        for(int i = 0; i < 10; i++) {
            double x = random.nextGaussian() * motion;
            double y = random.nextGaussian() * motion;
            double z = random.nextGaussian() * motion;
            level().addParticle(ParticleTypes.SMOKE, getRandomX(1.0F), getRandomY() + (double)0.5F, getRandomZ(1.0F), x, y, z);
        }
    }

    protected void spawnStompParticles() {
        float forwardsYaw = yBodyRot - 180F;
        float x = Mth.sin(-forwardsYaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float z = Mth.cos(-forwardsYaw * (float) (Math.PI / 180.0) - (float) Math.PI);
        float offset = getBbWidth()/2f;
        var stompPosition = position().add(x*offset, 0, z*offset);
        var pos = BlockPos.containing(Math.round(stompPosition.x), Math.round(stompPosition.y)-1, Math.round(stompPosition.z));
        var state = level().getBlockState(pos);
        var particle = new BlockParticleOption(ParticleTypes.DUST_PILLAR, state);
        level().levelEvent(2001, pos, Block.getId(state));

        for (int i = 0; i < 40; i++) {
            double radialOffset = 0.75f;
            double xPos = stompPosition.x + radialOffset * Math.cos(i) + random.nextGaussian() / 2.0;
            double yPos = stompPosition.y + 0.1f;
            double zPos = stompPosition.z + radialOffset * Math.sin(i) + random.nextGaussian() / 2.0;
            double xVelocity = random.nextGaussian() * 0.05F;
            double yVelocity = random.nextGaussian() * 0.075F;
            double zVelocity = random.nextGaussian() * 0.05F;
            level().addParticle(particle, xPos, yPos, zPos, xVelocity, yVelocity, zVelocity);
        }
    }

    /**
     * Copied From & Matches {@link net.minecraft.world.entity.animal.horse.AbstractHorse}
     */
    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        if (isSaddled()) {
            if (getFirstPassenger() instanceof Player player) {
                return player;
            }
        }

        return super.getControllingPassenger();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NMLSounds.MOOSE_AMBIENT.get();
    }
    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return NMLSounds.MOOSE_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NMLSounds.MOOSE_DEATH.get();
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        playSound(NMLSounds.MOOSE_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }

    public int getPacificationStage() {
        return pacificationStage;
    }

    public void setPacificationStage(int stage) {
        pacificationStage = stage;
    }

    public boolean isPacified() {
        return getPacificationStage() == MINIMUM_TAME_ATTEMPTS;
    }

    @Override
    public boolean isSaddled() {
        return entityData.get(DATA_IS_SADDLED);
    }

    public void setIsSaddled(boolean isSaddled) {
        entityData.set(DATA_IS_SADDLED, isSaddled);
    }
}