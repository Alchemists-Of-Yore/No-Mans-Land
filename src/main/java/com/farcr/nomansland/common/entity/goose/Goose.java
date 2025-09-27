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
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
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

import java.util.function.IntFunction;

public class Goose extends Animal {

    private static final EntityDataAccessor<State> DATA_STATE = SynchedEntityData.defineId(Goose.class, NMLEntityDataSerializers.GOOSE_STATE.get());
    private int hurtAnimationTick = 0;
    public final AnimationState hurtingAnimationState = new AnimationState();
    public final AnimationState fallingAnimationState = new AnimationState();
    public final AnimationState intimidatingAnimationState = new AnimationState();

    public Goose(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.ATTACK_DAMAGE, 1)
                .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return NMLEntities.GOOSE.get().create(serverLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_STATE, State.IDLING);
        super.defineSynchedData(builder);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
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
                if (angryAt == player.getUUID()) {
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

        level.getProfiler().push("gooseActivityUpdate");
        GooseAI.updateActivity(this);
        level.getProfiler().pop();

        super.customServerAiStep();
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

    @Override
    public PathNavigation getNavigation() {
        return super.getNavigation();
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
