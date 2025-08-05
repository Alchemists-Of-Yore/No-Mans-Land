package com.farcr.nomansland.common.entity.tortoise.ai;

import com.farcr.nomansland.common.block.TortoiseEggBlock;
import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class TortoiseLayEggGoal extends Goal {
    protected final Tortoise tortoise;
    private final double speedModifier;
    private final Level level;
    private boolean failedAttempt = false;
    protected long tryAgainTime = 0;
    private Path path;

    public TortoiseLayEggGoal(Tortoise mob, double speedModifier) {
        this.tortoise = mob;
        this.speedModifier = speedModifier;
        this.level = mob.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        long gameTime = this.tortoise.level().getGameTime();
        return this.tortoise.hasEgg() && !this.tortoise.inShell() && this.getHidePos() != null && (gameTime - this.tryAgainTime > 200L);
    }

    @Override
    public boolean canContinueToUse() {
        return this.tortoise.hasEgg() && !this.tortoise.inShell() && !this.failedAttempt;
    }

    @Override
    public void tick() {
        if (this.path == null)
            return;
        BlockPos blockToGo = this.path.getTarget();
        this.tortoise.getLookControl().setLookAt(Vec3.atCenterOf(blockToGo));
        this.tortoise.getNavigation().moveTo(path, this.speedModifier);
        if (!tortoise.isValidHome(blockToGo))
            this.failedAttempt = true;
        if (path.isDone() && this.tortoise.isValidHome(blockToGo) && this.tortoise.isValidHome(this.tortoise.blockPosition())) {
            this.tortoise.getNavigation().stop();
            this.tortoise.stopInPlace();
            if (this.tortoise.getLayEggCounter() < 1) {
                this.tortoise.setLayingEgg(true);
                Level level = this.tortoise.level();
                level.playSound(null, this.tortoise.blockPosition(), SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + level.random.nextFloat() * 0.2F);
                BlockState blockstate = NMLBlocks.TORTOISE_EGGS.get()
                        .defaultBlockState()
                        .setValue(TortoiseEggBlock.EGGS, Integer.valueOf(this.tortoise.getRandom().nextInt(3) + 1));
                level.setBlock(this.tortoise.blockPosition(), blockstate, 3);
                level.gameEvent(GameEvent.BLOCK_PLACE, this.tortoise.blockPosition(), GameEvent.Context.of(this.tortoise, blockstate));
                this.tortoise.setHasEgg(false);
                this.tortoise.setLayingEgg(false);
                this.tortoise.setInLoveTime(600);
                this.tortoise.setHomePos(this.tortoise.blockPosition());
            }
            if (this.tortoise.isLayingEgg()) {
                this.tortoise.setLayEggCounter(this.tortoise.getLayEggCounter() + 1);
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (this.failedAttempt) {
            this.tryAgainTime = this.tortoise.level().getGameTime();
            this.failedAttempt = false;
        }
    }

    @Nullable
    protected BlockPos getHidePos() {
        if (!tortoise.hasEgg())
            return null;
        // Borrowed from TryToFindWaterGoal, modified to have a longer range and accomodate the Tortoise's larger hitbox
        Iterable<BlockPos> iterable = BlockPos.betweenClosed(Mth.floor(tortoise.getX() - 20), Mth.floor(tortoise.getY() - 10), Mth.floor(tortoise.getZ() - 20), Mth.floor(tortoise.getX() + 20), Mth.floor(tortoise.getY() + 10), Mth.floor(tortoise.getZ() + 20));
        BlockPos blockToGo = null;
        for (BlockPos newPos : iterable) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos horizontalPos = newPos.relative(direction);
                if (level.isEmptyBlock(horizontalPos.above()) &&
                        level.getBlockState(horizontalPos).isPathfindable(PathComputationType.LAND)
                        && tortoise.isValidHome(horizontalPos) &&
                        BlockPos.squareOutSouthEast(horizontalPos).allMatch(blockPos -> tortoise.isValidHome(blockPos))) {
                    blockToGo = horizontalPos;
                    break;
                }
            }
            if (blockToGo != null) {
                this.path = tortoise.getNavigation().createPath(blockToGo, 0);
                return blockToGo;
            }
        }
        return null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
