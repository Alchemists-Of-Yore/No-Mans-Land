package com.farcr.nomansland.common.entity.goose;

import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.entities.NMLEntityDataSerializers;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntFunction;

public class Goose extends Animal {

    private static final EntityDataAccessor<State> DATA_STATE = SynchedEntityData.defineId(Goose.class, NMLEntityDataSerializers.GOOSE_STATE.get());
    private static final EntityDataAccessor<ItemStack> DATA_CARRIED_ITEM = SynchedEntityData.defineId(Goose.class, EntityDataSerializers.ITEM_STACK);
    private int hurtAnimationTick = 0;
    public final AnimationState hurtingAnimationState = new AnimationState();
    public final AnimationState fallingAnimationState = new AnimationState();
    public final AnimationState intimidatingAnimationState = new AnimationState();

    private static final double FLOCK_RADIUS = 12.0;
    private static final int FLAP_DISPLAY_TICKS = 12;
    private static final long ATTACK_TARGET_EXPIRY = 240L;

    private final GooseGrudges grudges = new GooseGrudges();
    @Nullable
    private BlockPos aggressionAnchor;
    private long attackReadyAt;
    private int flapTicks;
    private boolean stealing;

    public Goose(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public GooseGrudges getGrudges() {
        return grudges;
    }

    @Nullable
    public BlockPos getAggressionAnchor() {
        return aggressionAnchor;
    }

    public void setAnchor(BlockPos pos) {
        aggressionAnchor = pos;
    }

    public ItemStack getCarriedItem() {
        return entityData.get(DATA_CARRIED_ITEM);
    }

    public void setCarriedItem(ItemStack stack) {
        entityData.set(DATA_CARRIED_ITEM, stack);
    }

    public boolean isCarrying() {
        return !getCarriedItem().isEmpty();
    }

    public boolean isStealing() {
        return stealing;
    }

    public void setStealing(boolean stealing) {
        this.stealing = stealing;
    }

    public void dropCarriedItem() {
        ItemStack carried = getCarriedItem();
        if (!carried.isEmpty()) {
            if (!level().isClientSide) spawnAtLocation(carried);
            setCarriedItem(ItemStack.EMPTY);
        }
    }

    public boolean isAttackReady() {
        return level().getGameTime() >= attackReadyAt;
    }

    public void setAttackCooldown(int ticks) {
        attackReadyAt = level().getGameTime() + ticks;
    }

    public void flapBriefly() {
        flapTicks = FLAP_DISPLAY_TICKS;
    }

    public List<Goose> nearbyGeese(double radius) {
        return level().getEntitiesOfClass(Goose.class, getBoundingBox().inflate(radius), other -> other != this && other.isAlive());
    }

    public int flockConfidence() {
        return nearbyGeese(FLOCK_RADIUS).size();
    }

    public void beginAttack(LivingEntity target) {
        aggressionAnchor = blockPosition();
        Brain<Goose> brain = getBrain();
        brain.eraseMemory(MemoryModuleType.AVOID_TARGET);
        brain.setMemoryWithExpiry(MemoryModuleType.ATTACK_TARGET, target, ATTACK_TARGET_EXPIRY);
        setTarget(target);
    }

    public void rallyFlock(LivingEntity target) {
        for (Goose ally : nearbyGeese(FLOCK_RADIUS)) {
            if (ally.canFight() && !ally.isCarrying() && !ally.isStealing()
                    && ally.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).isEmpty()) {
                ally.beginAttack(target);
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    public static boolean checkGooseSpawnRules(EntityType<? extends Animal> animal, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        boolean flag = isBrightEnoughToSpawn(level, pos);
        return level.getBlockState(pos.above()).isAir() && flag;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return NMLEntities.GOOSE.get().create(serverLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_STATE, State.IDLING);
        builder.define(DATA_CARRIED_ITEM, ItemStack.EMPTY);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        grudges.save(compound);
        if (isCarrying()) {
            compound.put("CarriedItem", getCarriedItem().save(registryAccess()));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        grudges.load(compound);
        setCarriedItem(compound.contains("CarriedItem")
                ? ItemStack.parseOptional(registryAccess(), compound.getCompound("CarriedItem"))
                : ItemStack.EMPTY);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        dropCarriedItem();
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (reason.shouldDestroy()) dropCarriedItem();
        super.remove(reason);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.PUMPKIN_SEEDS);
    }

    @Override
    public void setInLove(@Nullable Player player) {
        super.setInLove(player);

        if (player != null) {
            getBrain().getMemory(MemoryModuleType.ANGRY_AT).ifPresent(angryAt -> {
                if (angryAt.equals(player.getUUID())) {
                    getBrain().eraseMemory(MemoryModuleType.ANGRY_AT);
                }
            });

            getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
                if (target == player) {
                    getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                    setTarget(null);
                }
            });
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (DATA_STATE.equals(key)) {
            resetAnimations();
            if (getState() == State.INTIMIDATING) {
                intimidatingAnimationState.startIfStopped(tickCount);
            }
            refreshDimensions();
        }

        super.onSyncedDataUpdated(key);
    }

    private void resetAnimations() {
        intimidatingAnimationState.stop();
    }

    public State getState() {
        return entityData.get(DATA_STATE);
    }

    public boolean showWings() {
        return getState() != State.IDLING || hurtingAnimationState.isStarted() || fallingAnimationState.isStarted();
    }

    public void setState(State state) {
        entityData.set(DATA_STATE, state);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    protected Brain.Provider<Goose> brainProvider() {
        return GooseAI.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return GooseAI.makeBrain(brainProvider().makeBrain(dynamic));
    }

    @Override
    public Brain<Goose> getBrain() {
        return (Brain<Goose>) super.getBrain();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NMLSounds.GOOSE_AMBIENT.get();
    }
    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return NMLSounds.GOOSE_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NMLSounds.GOOSE_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        playSound(NMLSounds.GOOSE_STEP.get(), 0.15F, 1);
    }

    public boolean canFight() {
        return !isBaby() && getHealth() > getMaxHealth() / 2;
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) level();

        level.getProfiler().push("gooseBrain");
        getBrain().tick(level, this);
        level.getProfiler().pop();

        GooseAI.updateActivity(this);
        updateState();

        super.customServerAiStep();
    }

    // Animation state is derived in one place so the behaviours never fight over it.
    private void updateState() {
        Brain<Goose> brain = getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            setState(State.RUNNING);
        } else if (brain.hasMemoryValue(MemoryModuleType.AVOID_TARGET)) {
            setState(canFight() ? State.INTIMIDATING : State.RUNNING);
        } else if (isCarrying()) {
            setState(State.IDLING);
        } else if (flapTicks > 0) {
            setState(State.INTIMIDATING);
        } else {
            setState(State.IDLING);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();

        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < 0) {
            this.setDeltaMovement(vec3.multiply(1, 0.6, 1));
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 4) {
            hurtingAnimationState.start(tickCount);
            hurtAnimationTick = 22;
        }

        super.handleEntityEvent(id);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        level().broadcastEntityEvent(this, (byte) 4);
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        super.tick();

        if (onGround() || getDeltaMovement().y > 0) fallingAnimationState.ifStarted(AnimationState::stop);
        else fallingAnimationState.startIfStopped(tickCount);
        
        if (hurtAnimationTick > 0) hurtAnimationTick--;
        else hurtingAnimationState.ifStarted(AnimationState::stop);

        if (!level().isClientSide && flapTicks > 0) flapTicks--;

        floatGoose();
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getFluidState(pos).is(FluidTags.WATER) ? 5 : level.getPathfindingCostFromLightLevels(pos);
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState) {
        return fluidState.is(FluidTags.WATER);
    }

    private void floatGoose() {
        if (isInWater()) {
            CollisionContext collisioncontext = CollisionContext.of(this);
            if (collisioncontext.isAbove(LiquidBlock.STABLE_SHAPE, blockPosition(), true) && !level().getFluidState(blockPosition().above()).is(FluidTags.WATER)) {
                if (random.nextFloat() < 0.2F) setDeltaMovement(getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0));
                else setOnGround(true);
            } else {
                setDeltaMovement(getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0));
            }
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GoosePathNavigation(this, level);
    }

    public static class GoosePathNavigation extends GroundPathNavigation {
        GoosePathNavigation(Goose goose, Level level) {
            super(goose, level);
        }

        protected PathFinder createPathFinder(int maxVisitedNodes) {
            nodeEvaluator = new WalkNodeEvaluator();
            nodeEvaluator.setCanPassDoors(true);
            return new PathFinder(nodeEvaluator, maxVisitedNodes);
        }

        protected boolean hasValidPathType(PathType pathType) {
            return pathType == PathType.WATER || super.hasValidPathType(pathType);
        }

        public boolean isStableDestination(BlockPos pos) {
            return level.getBlockState(pos).is(Blocks.WATER) || super.isStableDestination(pos);
        }
    }

    public enum State {
        IDLING(0),
        INTIMIDATING(1),
        RUNNING(2);

        public static final IntFunction<State> BY_ID = ByIdMap.continuous(State::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, State> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, State::id);
        private final int id;

        State(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }
    }
}
