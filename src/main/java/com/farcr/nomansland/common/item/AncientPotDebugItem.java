package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.block.pots.PotBlock;
import com.farcr.nomansland.common.block.pots.PotModifier;
import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.world.saved_data.RegeneratingPotsData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AncientPotDebugItem extends Item {

    private static final PotModifier[] MODIFIERS = PotModifier.values();

    public AncientPotDebugItem(Properties properties) {
        super(properties);
    }

    private int getSelectedIndex(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag().getInt("SelectedModifier") % MODIFIERS.length;
    }

    private void setSelectedIndex(ItemStack stack, int index) {
        stack.update(DataComponents.CUSTOM_DATA, CustomData.EMPTY, existing -> {
            CompoundTag tag = existing.copyTag();
            tag.putInt("SelectedModifier", index);
            return CustomData.of(tag);
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (level.isClientSide || player == null) return InteractionResult.sidedSuccess(true);

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof PotBlock)) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof PotBlockEntity pot)) return InteractionResult.PASS;

        PotModifier modifier = MODIFIERS[getSelectedIndex(context.getItemInHand())];

        if (pot.hasModifier(modifier)) {
            pot.removeModifier(modifier);
            message(player, modifier.getSerializedName() + ": OFF", ChatFormatting.RED);
        } else {
            pot.addModifier(modifier);
            message(player, modifier.getSerializedName() + ": ON", ChatFormatting.GREEN);
        }

        pot.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !(state.getBlock() instanceof PotBlock);
    }

    public void handleLeftClick(Level level, BlockPos pos, Player player, ItemStack stack) {
        if (!(level.getBlockEntity(pos) instanceof PotBlockEntity pot)) return;

        if (player.isShiftKeyDown()) {
            pot.variant = null;
            level.destroyBlock(pos, false);
            if (level instanceof ServerLevel serverLevel) {
                RegeneratingPotsData.getOrDefault(serverLevel).removePot(pos);
            }
            message(player, "pot removed (no regeneration)", ChatFormatting.YELLOW);
            return;
        }

        int next = (getSelectedIndex(stack) + 1) % MODIFIERS.length;
        setSelectedIndex(stack, next);

        PotModifier modifier = MODIFIERS[next];
        boolean active = pot.hasModifier(modifier);
        message(player, "selected: " + modifier.getSerializedName() + (active ? " [ON]" : " [OFF]"),
                active ? ChatFormatting.GREEN : ChatFormatting.GRAY);
    }

    private static void message(Player player, String text, ChatFormatting color) {
        player.displayClientMessage(Component.literal(text).withStyle(color), true);
    }
}
