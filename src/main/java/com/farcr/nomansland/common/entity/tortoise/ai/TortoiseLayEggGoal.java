package com.farcr.nomansland.common.entity.tortoise.ai;

import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class TortoiseLayEggGoal extends MoveToBlockGoal {
    private final Tortoise tortoise;

    public TortoiseLayEggGoal(Tortoise turtle, double speedModifier) {
        super(turtle, speedModifier, 16);
        this.tortoise = turtle;
    }

    @Override
    public boolean canUse() {
        return this.tortoise.hasEgg() && this.tortoise.getHomePos().closerToCenterThan(this.tortoise.position(), 9.0) ? super.canUse() : false;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.tortoise.hasEgg() && this.tortoise.getHomePos().closerToCenterThan(this.tortoise.position(), 9.0);
    }

    @Override
    public void tick() {
        super.tick();
        BlockPos blockpos = this.tortoise.blockPosition();
        if (!this.tortoise.isInWater() && this.isReachedTarget()) {
            if (this.tortoise.layEggCounter < 1) {
                this.tortoise.setLayingEgg(true);
            } else if (this.tortoise.layEggCounter > this.adjustedTickDelay(200)) {
                Level level = this.tortoise.level();
                level.playSound(null, blockpos, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + level.random.nextFloat() * 0.2F);
                BlockPos blockpos1 = this.blockPos.above();
                BlockState blockstate = Blocks.TURTLE_EGG
                        .defaultBlockState()
                        .setValue(TurtleEggBlock.EGGS, Integer.valueOf(this.tortoise.getRandom().nextInt(4) + 1));
                level.setBlock(blockpos1, blockstate, 3);
                level.gameEvent(GameEvent.BLOCK_PLACE, blockpos1, GameEvent.Context.of(this.tortoise, blockstate));
                this.tortoise.setHasEgg(false);
                this.tortoise.setLayingEgg(false);
                this.tortoise.setInLoveTime(600);
            }

            if (this.tortoise.isLayingEgg()) {
                this.tortoise.layEggCounter++;
            }
        }
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        return !level.isEmptyBlock(pos.above()) ? false : TurtleEggBlock.isSand(level, pos);
    }
}
