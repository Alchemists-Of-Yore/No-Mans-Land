package com.farcr.nomansland.common.entity.tortoise.ai;

import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
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

    public TortoiseLayEggGoal(Tortoise mob, double speedModifier) {
        this.tortoise = mob;
        this.speedModifier = speedModifier;
        this.level = mob.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.getHidePos() != null && this.tortoise.hasEgg();
    }

    @Override
    public boolean canContinueToUse() {
        return this.tortoise.hasEgg();
    }

    @Override
    public void tick() {
        BlockPos blockpos = this.tortoise.getHomePos();
        if (blockpos == null)
            return;
        this.tortoise.getLookControl().setLookAt(Vec3.atCenterOf(blockpos));
        if (this.tortoise.isValidHome(this.tortoise.blockPosition())) {
            this.tortoise.setHomePos(this.tortoise.blockPosition());
        } else {
            Path path = this.tortoise.getNavigation().createPath(blockpos, 0);
            if (path != null && path.canReach())
                this.tortoise.getNavigation().moveTo(path, speedModifier);
            else
                this.getHidePos();
        }
        if (blockpos.closerThan(tortoise.blockPosition(), 1.0D)) {
            this.tortoise.getNavigation().stop();
            if (this.tortoise.getLayEggCounter() < 1) {
                this.tortoise.setLayingEgg(true);
                Level level = this.tortoise.level();
                level.playSound(null, blockpos, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + level.random.nextFloat() * 0.2F);
                BlockState blockstate = Blocks.TURTLE_EGG
                        .defaultBlockState()
                        .setValue(TurtleEggBlock.EGGS, Integer.valueOf(this.tortoise.getRandom().nextInt(4) + 1));
                level.setBlock(blockpos, blockstate, 3);
                level.gameEvent(GameEvent.BLOCK_PLACE, blockpos, GameEvent.Context.of(this.tortoise, blockstate));
                this.tortoise.setHasEgg(false);
                this.tortoise.setLayingEgg(false);
                this.tortoise.setInLoveTime(600);
            }
            if (this.tortoise.isLayingEgg()) {
                this.tortoise.layEggCounter++;
            }
        }
    }

    @Nullable
    protected BlockPos getHidePos() {
        RandomSource randomsource = this.tortoise.getRandom();
        BlockPos currentPosition = this.tortoise.blockPosition();
        for (int i = 0; i < 10; i++) {
            // If I'm not at a dark spot, my priority is to go there, if I'm already at a dark spot, my priority is to go to the correct spot
            BlockPos newPosition = currentPosition.offset(randomsource.nextInt(20) - 10, randomsource.nextInt(6) - 3, randomsource.nextInt(20) - 10);
            boolean requirements = level.getRawBrightness(currentPosition, 0) < 13 ? tortoise.isValidHome(newPosition) : level.getRawBrightness(newPosition, 0) < 13 && !level.canSeeSky(newPosition);
            if (requirements && level.isEmptyBlock(newPosition.above())) {
                this.tortoise.setHomePos(newPosition);
                return newPosition;
            }
        }
        return null;
    }
}
