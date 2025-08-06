package com.farcr.nomansland.common.entity.tortoise.ai;

import com.farcr.nomansland.common.block.TortoiseEggBlock;
import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class TortoiseLayEggGoal extends Goal {
    private Tortoise tortoise;

    public TortoiseLayEggGoal(Tortoise tortoise) {
        this.tortoise = tortoise;
    }

    @Override
    public boolean canUse() {
        return this.tortoise.isLayingEgg();
    }

    @Override
    public boolean canContinueToUse() {
        return this.tortoise.isLayingEgg();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.tortoise.isValidHome(this.tortoise.blockPosition())) {
            this.tortoise.stopInPlace();
            if (this.tortoise.getLayEggCounter() > this.adjustedTickDelay(100)) {
                Level level = this.tortoise.level();
                level.playSound(null, this.tortoise.blockPosition(), SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + level.random.nextFloat() * 0.2F);
                BlockState blockstate = NMLBlocks.TORTOISE_EGGS.get()
                        .defaultBlockState()
                        .setValue(TortoiseEggBlock.EGGS, Integer.valueOf(this.tortoise.getRandom().nextInt(3) + 1));
                level.setBlock(this.tortoise.blockPosition(), blockstate, 3);
                level.gameEvent(GameEvent.BLOCK_PLACE, this.tortoise.blockPosition(), GameEvent.Context.of(this.tortoise, blockstate));
                this.tortoise.setHasEgg(false);
                this.tortoise.setInLoveTime(600);
                this.tortoise.setHomePos(this.tortoise.blockPosition());
                this.tortoise.setLayingEgg(false);
            }
            if (this.tortoise.isLayingEgg()) {
                this.tortoise.setLayEggCounter(this.tortoise.getLayEggCounter() + 1);
            }
        }
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
