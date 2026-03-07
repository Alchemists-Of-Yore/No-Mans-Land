package com.farcr.nomansland.common.entity.living_pot;

import com.farcr.nomansland.common.block.pots.PotTrait;
import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

public class LivingPotReturnHomeGoal extends Goal {

    private static final double ARRIVE_RANGE_SQ = 1.5 * 1.5;
    private static final int SLEEP_DURATION = 25;

    private enum Phase { WALKING, SLEEPING }

    private final LivingPot pot;
    private Phase phase = Phase.WALKING;
    private int sleepTicks = 0;

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
                pot.getNavigation().stop();
                pot.setSleeping(true);
            }
        } else {
            sleepTicks++;
            if (sleepTicks >= SLEEP_DURATION) {
                pot.setSleeping(false);
                BlockPos home = pot.getHomePos();
                if (home != null) {
                    placeBlockAt(pot.level(), choosePlacePos(home));
                }
                pot.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    @Override
    public void stop() {
        pot.setSleeping(false);
        phase = Phase.WALKING;
        sleepTicks = 0;
    }

    private BlockPos choosePlacePos(BlockPos home) {
        Level level = pot.level();
        if (!level.getBlockState(home).isAir() && !level.getBlockState(home).canBeReplaced()) {
            return pot.blockPosition();
        }
        return home;
    }

    private void placeBlockAt(Level level, BlockPos pos) {
        if (pot.variant == null) return;

        BlockState state = pot.blockState;
        level.setBlockAndUpdate(pos, state);

        if (level.getBlockEntity(pos) instanceof PotBlockEntity be) {
            be.variant = pot.variant;
            if (pot.getPotLootTable() != null) {
                be.setLootTable(pot.getPotLootTable());
                be.setLootTableSeed(pot.getLootTableSeed());
            } else if (!pot.storedItem.isEmpty()) {
                be.setTheItem(pot.storedItem);
            }
        }
        if (pot.variant.traits().contains(PotTrait.TRAPPED) && !level.isClientSide()) {
            level.scheduleTick(pos, state.getBlock(), 2);
        }
    }
}
