package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.entity.bombs.ThrowableBombEntity;
import com.farcr.nomansland.common.item.ThrowableBombItem;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;

public class BombDispenseBehavior extends DefaultDispenseItemBehavior {

    private final ThrowableBombItem bombItem;

    public BombDispenseBehavior(Item bomb) {
        if (bomb instanceof ThrowableBombItem throwableBombItem) {
            this.bombItem = throwableBombItem;
        } else {
            String name = String.valueOf(bomb);
            throw new IllegalArgumentException(name + " not instance of " + ThrowableBombItem.class.getSimpleName());
        }
    }

    public ItemStack execute(BlockSource source, ItemStack stack) {
        Level level = source.level();
        ProjectileItem.DispenseConfig dispenseConfig = bombItem.createDispenseConfig();
        Direction direction = source.state().getValue(DispenserBlock.FACING);
        Position position = dispenseConfig.positionFunction().getDispensePosition(source, direction);
        ThrowableBombEntity bombEntity = bombItem.asProjectile(level, position, stack, direction);
        bombEntity.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), dispenseConfig.power(), dispenseConfig.uncertainty());
        level.addFreshEntity(bombEntity);
        stack.shrink(1);
        return stack;
    }

    protected void playSound(BlockSource source) {
        source.level().levelEvent(1002, source.pos(), 0);
    }
}
