package com.farcr.nomansland.common.entity.living_pot;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class LivingPotReturnHomeGoal extends Goal {

    private static final double ARRIVE_RANGE_SQ = 1.5 * 1.5;
    private static final int SLEEP_DURATION = 25;

    private enum Phase { WALKING, SLEEPING }

    private final LivingPot pot;
    private Phase phase = Phase.WALKING;
    private int sleepTicks = 0;
    private BlockPos targetPos;

    public LivingPotReturnHomeGoal(LivingPot pot) {
        this.pot = pot;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return pot.getReturnTimer() <= 0 && pot.isIdle();
    }

    @Override
    public boolean canContinueToUse() {
        if (!pot.isIdle()) return false;
        if (phase == Phase.SLEEPING) return sleepTicks < SLEEP_DURATION;
        BlockPos home = pot.getHomePos();
        return home == null || pot.distanceToSqr(home.getX() + 0.5, home.getY(), home.getZ() + 0.5) > ARRIVE_RANGE_SQ;
    }

    @Override
    public void start() {
        phase = Phase.WALKING;
        sleepTicks = 0;
        BlockPos home = pot.getHomePos();
        if (home != null) {
            pot.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1.0);
        }
    }

    @Override
    public void tick() {
        if (phase == Phase.WALKING) {
            BlockPos home = pot.getHomePos();
            if (home == null) home = new BlockPos(pot.getBlockX(), pot.getBlockY(), pot.getBlockZ());

            pot.getNavigation().moveTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 1.0);

            if (pot.distanceToSqr(home.getX() + 0.5, home.getY(), home.getZ() + 0.5) <= ARRIVE_RANGE_SQ || pot.getNavigation().isDone()) {
                phase = Phase.SLEEPING;
                sleepTicks = 0;
                BlockPos placePos = choosePlacePos(home);
                pot.setPos(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5);
                pot.setDeltaMovement(0, 0, 0);
                pot.getNavigation().stop();
                pot.setSleeping(true);
                this.targetPos = placePos;
            }
        } else {
            pot.getNavigation().stop();
            pot.setDeltaMovement(0, pot.getDeltaMovement().y, 0);
            sleepTicks++;
            if (sleepTicks >= SLEEP_DURATION) {
                pot.setSleeping(false);
                if (targetPos != null) {
                    placeBlockAt(targetPos);
                }
            }
        }
    }

    @Override
    public void stop() {
        pot.setSleeping(false);
        phase = Phase.WALKING;
        sleepTicks = 0;
        targetPos = null;
    }

    private BlockPos choosePlacePos(BlockPos home) {
        Level level = pot.level();
        if (canPlaceAt(level, home)) return home;

        for (int r = 1; r <= 3; r++) {
            for (BlockPos pos : BlockPos.betweenClosed(home.offset(-r, -1, -r), home.offset(r, 1, r))) {
                if (canPlaceAt(level, pos)) {
                    pot.setHomePos(pos.immutable());
                    return pos.immutable();
                }
            }
        }

        BlockPos fallback = pot.blockPosition();
        pot.setHomePos(fallback);
        return fallback;
    }

    private boolean canPlaceAt(Level level, BlockPos pos) {
        return (level.getBlockState(pos).isAir() || level.getBlockState(pos).canBeReplaced())
                && level.getBlockState(pos.below()).isSolid();
    }

    private void placeBlockAt(BlockPos pos) {
        pot.placeAsBlock(pos);
    }
}
