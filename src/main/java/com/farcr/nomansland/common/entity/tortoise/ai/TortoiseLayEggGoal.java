package com.farcr.nomansland.common.entity.tortoise.ai;

import com.farcr.nomansland.common.block.TortoiseEggBlock;
import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class TortoiseLayEggGoal extends Goal {
    protected final Tortoise tortoise;
    private final double speedModifier;
    private final Level level;
    private BlockPos blockToGo;
    private int failedAttempts;
    protected long tryAgainTime;

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
        return this.tortoise.hasEgg() && !this.tortoise.getNavigation().isDone() && !this.tortoise.inShell() && this.failedAttempts <= 20;
    }

    @Override
    public void tick() {
        if (this.blockToGo == null)
            return;
        this.tortoise.getLookControl().setLookAt(Vec3.atCenterOf(this.blockToGo));
        Path path = this.tortoise.getNavigation().createPath(this.blockToGo, 0);
        if (path != null && path.canReach()) {
            this.tortoise.getNavigation().moveTo(path, this.speedModifier);
        } else {
            this.failedAttempts++;
            this.getHidePos();
        }
        if (this.blockToGo.closerThan(this.tortoise.blockPosition(), 1.0D) && this.tortoise.isValidHome(blockToGo) || this.tortoise.isValidHome(this.tortoise.blockPosition())) {
            this.tortoise.getNavigation().stop();
            this.blockToGo = tortoise.blockPosition();
            if (this.tortoise.getLayEggCounter() < 1) {
                this.tortoise.setLayingEgg(true);
                Level level = this.tortoise.level();
                level.playSound(null, this.blockToGo, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + level.random.nextFloat() * 0.2F);
                BlockState blockstate = NMLBlocks.TORTOISE_EGGS.get()
                        .defaultBlockState()
                        .setValue(TortoiseEggBlock.EGGS, Integer.valueOf(this.tortoise.getRandom().nextInt(3) + 1));
                level.setBlock(this.blockToGo, blockstate, 3);
                level.gameEvent(GameEvent.BLOCK_PLACE, this.blockToGo, GameEvent.Context.of(this.tortoise, blockstate));
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
        if (this.failedAttempts >= 20) {
            this.tryAgainTime = this.tortoise.level().getGameTime();
            this.failedAttempts = 0;
        }
    }

    @Nullable
    protected BlockPos getHidePos() {
        if (!tortoise.hasEgg())
            return null;
        RandomSource randomsource = this.tortoise.getRandom();
        BlockPos currentPosition = this.tortoise.blockPosition();
        for (int i = 0; i < 10; i++) {
            // If I'm not at a dark spot, my priority is to go there, if I'm already at a dark spot, my priority is to go to the correct spot
            BlockPos newPosition = currentPosition.offset(randomsource.nextInt(20) - 10, randomsource.nextInt(6) - 3, randomsource.nextInt(20) - 10);
            boolean requirements = level.getRawBrightness(currentPosition, 0) < 13 ? tortoise.isValidHome(newPosition) : level.getRawBrightness(newPosition, 0) < 13 && !level.canSeeSky(newPosition);
            if ((level.getBlockState(newPosition).is(NMLBlocks.TORTOISE_EGGS) && tortoise.isValidHome(newPosition.below()) || requirements) && level.isEmptyBlock(newPosition.above())) {
                this.blockToGo = newPosition;
                return newPosition;
            }
        }
        return null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
