package com.farcr.nomansland.common.entity.beetle;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class BeetleFlyToReachGoal extends Goal {
    private final Beetle beetle;
    private double targetX;
    private double targetY;
    private double targetZ;

    public BeetleFlyToReachGoal(Beetle beetle) {
        this.beetle = beetle;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (beetle.getState() != Beetle.STATE_IDLE) return false;
        if (beetle.getFlightCooldown() > 0) return false;
        if (beetle.getDungBall() != null) return false;
        if (beetle.getRandom().nextInt(160) != 0) return false;
        BlockPos spot = findUnreachableSpot();
        if (spot == null) return false;
        targetX = spot.getX() + 0.5;
        targetY = spot.getY();
        targetZ = spot.getZ() + 0.5;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        beetle.startFlight(targetX, targetY, targetZ);
    }

    @Nullable
    private BlockPos findUnreachableSpot() {
        for (int attempt = 0; attempt < 6; attempt++) {
            int dx = beetle.getRandom().nextInt(17) - 8;
            int dz = beetle.getRandom().nextInt(17) - 8;
            if (Math.abs(dx) < 3 && Math.abs(dz) < 3) continue;
            BlockPos stand = findStandable(beetle.blockPosition().offset(dx, 0, dz));
            if (stand == null) continue;
            Path path = beetle.getNavigation().createPath(stand, 0);
            if (path == null || !path.canReach()) {
                return stand;
            }
        }
        return null;
    }

    @Nullable
    private BlockPos findStandable(BlockPos around) {
        Level level = beetle.level();
        for (int y = 4; y >= -4; y--) {
            BlockPos pos = around.offset(0, y, 0);
            BlockPos below = pos.below();
            if (level.getBlockState(below).isSolidRender(level, below)
                    && level.getBlockState(pos).isAir()
                    && level.getBlockState(pos.above()).isAir()) {
                return pos;
            }
        }
        return null;
    }
}
