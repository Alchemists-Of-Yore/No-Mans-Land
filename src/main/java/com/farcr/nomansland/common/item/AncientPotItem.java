package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.block.pots.PotSize;
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
    private final PotSize size;

    public AncientPotItem(PotSize size, Block block, Properties properties) {
        super(block, properties);
        this.size = size;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        ResourceLocation variantLocation = stack.get(NMLDataComponents.POT_VARIANT);
        if (variantLocation != null) {
            if (context.registries() != null) {
                HolderLookup.RegistryLookup<PotVariant> variantRegistry = context.registries().lookupOrThrow(NMLRegistries.POT_VARIANT_KEY);
                PotVariant variant = variantRegistry.get(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, variantLocation)).orElseThrow().value();
                variant.traits().forEach(trait -> {
                    String[] words = trait.getSerializedName().split("_");
                    for (int i = 0; i < words.length; i++) {
                        String word = words[i];
                        word = Character.toUpperCase(word.charAt(0)) + word.substring(1);
                        words[i] = word;
                    }
                    tooltipComponents.add(Component.literal(String.join(" ", words)).withStyle(ChatFormatting.DARK_GREEN));
                });
            } else tooltipComponents.add(Component.literal(variantLocation.toString()));
        }
    }

}
