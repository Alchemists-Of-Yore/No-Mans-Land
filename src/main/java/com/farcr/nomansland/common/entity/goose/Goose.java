package com.farcr.nomansland.common.entity.goose;

import com.farcr.nomansland.common.registry.NMLSounds;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;

public class Goose extends PathfinderMob {

    public Goose(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.ATTACK_DAMAGE, 2)
                .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
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
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
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
}
