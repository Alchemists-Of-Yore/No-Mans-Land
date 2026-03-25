package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class AncientPotItem extends BlockItem {

    public AncientPotItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (!tooltipFlag.isAdvanced()) return;
        ResourceLocation variantLocation = stack.get(NMLDataComponents.POT_VARIANT);
        if (variantLocation != null) {
            tooltipComponents.add(Component.literal(variantLocation.toString()).withStyle(ChatFormatting.DARK_GRAY));
            if (context.registries() != null) {
                HolderLookup.RegistryLookup<PotVariant> variantRegistry = context.registries().lookupOrThrow(NMLRegistries.POT_VARIANT_KEY);
                variantRegistry.get(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, variantLocation)).ifPresent(holder -> {
                    holder.value().traits().forEach(trait ->
                            tooltipComponents.add(Component.literal(trait.getSerializedName()).withStyle(ChatFormatting.DARK_GREEN)));
                });
            }
        }
    }

}
