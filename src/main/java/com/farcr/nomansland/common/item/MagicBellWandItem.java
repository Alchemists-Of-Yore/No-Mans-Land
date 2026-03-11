package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.blockentity.InvertedBellBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

// todo explote this
public class MagicBellWandItem extends Item {
    public MagicBellWandItem(Properties properties) {
        super(properties);
    }

    public static BlockPos linkedPos = null;
    @Override
    public InteractionResult useOn(final UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof InvertedBellBlockEntity ibbe) {
                InvertedBellBlockEntity controller = ibbe.getController();
                if (controller != null) {
                    if (linkedPos == null) {
                        linkedPos = controller.getBlockPos();
                    } else if (linkedPos != controller.getBlockPos()) {
                        controller.targetBell = linkedPos;
                        if (level.getBlockEntity(controller.targetBell) instanceof InvertedBellBlockEntity controller2) {
                            controller2.targetBell = controller.getBlockPos();
                        }
                        linkedPos = null;
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
