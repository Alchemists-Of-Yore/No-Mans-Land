package com.farcr.nomansland.common.integration.nirvana;

import com.farcr.nomansland.common.entity.bombs.ThrowableBombEntity;
import com.farcr.nomansland.common.item.ExplosiveItem;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FatJointItem extends ExplosiveItem {
    public FatJointItem(Properties properties) {
        super(properties);
    }

    @Override
    public ThrowableBombEntity asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        return new FatJoint(level, position.x(), position.y(), position.z());
    }
}
