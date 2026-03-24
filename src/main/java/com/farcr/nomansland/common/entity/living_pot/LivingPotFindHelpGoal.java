package com.farcr.nomansland.common.entity.living_pot;

import com.farcr.nomansland.common.block.pots.PotModifier;
import com.farcr.nomansland.common.block.pots.PotSize;
import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class LivingPotFindHelpGoal extends Goal {

    private static final int SEARCH_RADIUS = 20;
    private static final double WAKE_RANGE_SQ = 4.0; // 2 blocks away

    private final LivingPot pot;
    @Nullable private BlockPos targetPotPos;

    public LivingPotFindHelpGoal(LivingPot pot) {
        this.pot = pot;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!pot.isSmall() || !pot.isAngry() || pot.hasWokenLargePot) return false;
        targetPotPos = findNearestLargeLivingPotBlock();
        return targetPotPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!pot.isAngry() || pot.hasWokenLargePot) return false;
        if (targetPotPos == null) return false;
        return isLargeLivingPotBlock(targetPotPos);
    }

    @Override
    public void start() {
        if (targetPotPos != null) {
            pot.getNavigation().moveTo(
                    targetPotPos.getX() + 0.5, targetPotPos.getY(), targetPotPos.getZ() + 0.5, 1.4);
        }
    }

    @Override
    public void tick() {
        if (targetPotPos == null) return;

        pot.getNavigation().moveTo(
                targetPotPos.getX() + 0.5, targetPotPos.getY(), targetPotPos.getZ() + 0.5, 1.4);

        double distSq = pot.distanceToSqr(
                targetPotPos.getX() + 0.5, targetPotPos.getY(), targetPotPos.getZ() + 0.5);

        if (distSq <= WAKE_RANGE_SQ && isLargeLivingPotBlock(targetPotPos)) {
            Level level = pot.level();
            if (level.getBlockEntity(targetPotPos) instanceof PotBlockEntity be) {
                LivingEntity angryAt = pot.getPersistentAngerTarget() != null
                        ? (LivingEntity) level.getPlayerByUUID(pot.getPersistentAngerTarget())
                        : null;
                be.wakeUp(angryAt);
            }
            pot.hasWokenLargePot = true;
            pot.getNavigation().stop();
        }
    }

    @Override
    public void stop() {
        targetPotPos = null;
    }

    @Nullable
    private BlockPos findNearestLargeLivingPotBlock() {
        BlockPos origin = pot.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-SEARCH_RADIUS, -4, -SEARCH_RADIUS),
                origin.offset(SEARCH_RADIUS, 4, SEARCH_RADIUS))) {
            if (isLargeLivingPotBlock(pos)) {
                double d = pos.distSqr(origin);
                if (d < bestDist) {
                    bestDist = d;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }

    private boolean isLargeLivingPotBlock(BlockPos pos) {
        Level level = pot.level();
        if (!level.getBlockState(pos).is(NMLBlocks.LARGE_ANCIENT_POT.get())) return false;
        if (!(level.getBlockEntity(pos) instanceof PotBlockEntity be)) return false;
        return be.variant != null
                && be.variant.size() == PotSize.LARGE
                && be.hasModifier(PotModifier.ALIVE);
    }
}
