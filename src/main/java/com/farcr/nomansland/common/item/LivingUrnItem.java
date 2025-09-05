package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.entity.bombs.LivingUrn;
import com.farcr.nomansland.common.entity.bombs.ThrowableBombEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LivingUrnItem extends ThrowableBombItem {

    public LivingUrnItem(Properties properties) {
        super(properties);
    }

    @Override
    public ThrowableBombEntity asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new LivingUrn(level, position.x(), position.y(), position.z());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 3600;
    }
}