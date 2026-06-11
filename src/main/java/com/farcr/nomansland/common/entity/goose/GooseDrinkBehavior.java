package com.farcr.nomansland.common.entity.goose;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class GooseDrinkBehavior extends Behavior<Goose> {
    private static final int START_CHANCE = 180;

    @Nullable
    private Vec3 sipSpot;

    public GooseDrinkBehavior() {
        super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT), 60, 100);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
        if (goose.isCarrying() || goose.isStealing() || goose.isFlying()) return false;
        if (goose.getRandom().nextInt(START_CHANCE) != 0) return false;

        if (goose.isInWater()) {
            Vec3 ahead = goose.position().add(Vec3.directionFromRotation(0, goose.getYRot()).scale(1.2));
            sipSpot = new Vec3(ahead.x, goose.getY(), ahead.z);
            return true;
        }

        BlockPos water = adjacentWater(goose);
        if (water == null) return false;
        sipSpot = Vec3.atCenterOf(water);
        return true;
    }

    @Override
    protected void start(ServerLevel level, Goose goose, long gameTime) {
        goose.getNavigation().stop();
        goose.setDrinking(true);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
        return !goose.isFlying()
                && !goose.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)
                && !goose.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && !goose.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET);
    }

    @Override
    protected void tick(ServerLevel level, Goose goose, long gameTime) {
        if (sipSpot != null) {
            goose.getLookControl().setLookAt(sipSpot.x, sipSpot.y - 0.5, sipSpot.z);
        }
    }

    @Override
    protected void stop(ServerLevel level, Goose goose, long gameTime) {
        goose.setDrinking(false);
        sipSpot = null;
    }

    @Nullable
    private static BlockPos adjacentWater(Goose goose) {
        BlockPos feet = goose.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(feet.offset(-1, -1, -1), feet.offset(1, 0, 1))) {
            if (goose.level().getFluidState(pos).is(FluidTags.WATER)) return pos.immutable();
        }
        return null;
    }
}
