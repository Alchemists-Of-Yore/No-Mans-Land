package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.entity.bombs.Firebomb;
import com.farcr.nomansland.common.entity.bombs.ThrowableBombEntity;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FirebombItem extends ThrowableBombItem {

    public FirebombItem(Properties properties) {
        super(properties);
    }

    @Override
    public ThrowableBombEntity createBomb(LivingEntity entity, Level level) {
        return new Firebomb(entity, level);
    }

    @Override
    public ThrowableBombEntity createBomb(Level level, BlockPos pos) {
        return new Firebomb(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        super.onUseTick(level, entity, stack, remainingTicks);
        int timeUsed = this.getUseDuration(stack, entity) - remainingTicks;
        if (timeUsed == DEFAULT_THROW_TIME && entity.isShiftKeyDown()) entity.playSound(NMLSounds.BOMB_PRIMED.get());
    }
}