package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.entity.bombs.FirebombEntity;
import com.farcr.nomansland.common.entity.bombs.LivingUrnEntity;
import com.farcr.nomansland.common.entity.bombs.ThrowableBombEntity;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LivingUrnItem extends ThrowableBombItem {

    public LivingUrnItem(Properties properties) {
        super(properties);
    }

    @Override
    public ThrowableBombEntity createBomb(LivingEntity entity, Level level) {
        return new LivingUrnEntity(entity, level);
    }

    @Override
    public ThrowableBombEntity createBomb(Level level, BlockPos pos) {
        return new LivingUrnEntity(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 3600;
    }
}