package com.farcr.nomansland.common.entity.goose;

import com.farcr.nomansland.common.entity.Moose;
import com.farcr.nomansland.common.entity.MooseAI;
import com.farcr.nomansland.common.registry.NMLSounds;
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
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.IntFunction;

import static com.farcr.nomansland.common.entity.Moose.MooseState.STOMPING;
import static com.farcr.nomansland.common.entity.goose.Goose.GooseState.IDLE;

public class Goose extends PathfinderMob {

    private static final EntityDataAccessor<GooseState> GOOSE_STATE = SynchedEntityData.defineId(Goose.class, NMLEntityDataSerializers.GOOSE_STATE.get());
    private long inStateTicks = 0L;


    public Goose(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

//    @Override
//    protected void registerGoals() {
//        super.registerGoals();
//        goalSelector.addGoal(1, new PanicGoal(this, 1.65));
//        goalSelector.addGoal(2, new RandomStrollGoal(this, 1.0, 60));
//        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
//        goalSelector.addGoal(4, new GooseGoToWaterGoal(this, 1.0));
//        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
//        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Goose.class, 8.0F));
//    }


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);

        builder.define(GOOSE_STATE, GooseState.IDLE);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putString("State", this.getState().getSerializedName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        this.switchToState(GooseState.fromName(compound.getString("State")));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        if (key.equals(GOOSE_STATE)) {
            this.inStateTicks = 0L;
        }

        super.onSyncedDataUpdated(key);
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
        this.playSound(NMLSounds.GOOSE_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    protected void customServerAiStep() {
        ServerLevel level = (ServerLevel) this.level();

        level.getProfiler().push("gooseBrain");
        getBrain().tick(level, this);
        level.getProfiler().pop();

        level.getProfiler().push("gooseActivityUpdate");
        GooseAI.updateActivity(this);
        level.getProfiler().pop();

        super.customServerAiStep();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }

        this.inStateTicks++;
        floatGoose();
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState) {
        return fluidState.is(FluidTags.WATER);
    }

    private void floatGoose() {
        if (this.isInWater()) {
            CollisionContext collisioncontext = CollisionContext.of(this);
            if (collisioncontext.isAbove(LiquidBlock.STABLE_SHAPE, this.blockPosition(), true) && !level().getFluidState(blockPosition().above()).is(FluidTags.WATER)) {
                this.setOnGround(true);
            } else {
                this.setDeltaMovement(getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0));
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

    public GooseState getState() {
        return this.entityData.get(GOOSE_STATE);
    }

    public void switchToState(GooseState state) {
        this.entityData.set(GOOSE_STATE, state);
    }


    private void setupAnimationStates() {
        switch (this.getState()) {
            case IDLE:
                break;
            case DRINKING:
                break;
        }
    }

//    public static class GooseGoToWaterGoal extends MoveToBlockGoal {
//        private final Goose goose;
//
//        GooseGoToWaterGoal(Goose goose, double speedModifier) {
//            super(goose, speedModifier, 8, 2);
//            this.goose = goose;
//        }
//
//        public BlockPos getMoveToTarget() {
//            return this.blockPos;
//        }
//
//        public boolean canContinueToUse() {
//            return !this.goose.isInWater() && this.isValidTarget(this.goose.level(), this.blockPos);
//        }
//
//        public boolean canUse() {
//            return !this.goose.isInWater() && super.canUse();
//        }
//
//        public boolean shouldRecalculatePath() {
//            return this.tryTicks % 20 == 0;
//        }
//
//        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
//            return level.getBlockState(pos).is(Blocks.WATER) && level.getBlockState(pos.above()).isPathfindable(PathComputationType.LAND);
//        }
//    }
    
    public static class GoosePathNavigation extends GroundPathNavigation {
        GoosePathNavigation(Goose goose, Level level) {
            super(goose, level);
        }

        protected PathFinder createPathFinder(int maxVisitedNodes) {
            this.nodeEvaluator = new WalkNodeEvaluator();
            this.nodeEvaluator.setCanPassDoors(true);
            return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
        }

        protected boolean hasValidPathType(PathType pathType) {
            return pathType == PathType.WATER || super.hasValidPathType(pathType);
        }

        public boolean isStableDestination(BlockPos pos) {
            return this.level.getBlockState(pos).is(Blocks.WATER) || super.isStableDestination(pos);
        }
    }

    public enum GooseState implements StringRepresentable {
        IDLE("idle", 0),
        DRINKING("drinking", 1);
        
        private static final StringRepresentable.EnumCodec<GooseState> CODEC = StringRepresentable.fromEnum(GooseState::values);
        private static final IntFunction<GooseState> BY_ID = ByIdMap.continuous(
                GooseState::id, values(), ByIdMap.OutOfBoundsStrategy.ZERO
        );
        public static final StreamCodec<ByteBuf, GooseState> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, GooseState::id);
        private final String name;
        private final int id;

        GooseState(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public static GooseState fromName(String name) {
            return CODEC.byName(name, IDLE);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        private int id() {
            return this.id;
        }
    }
}
