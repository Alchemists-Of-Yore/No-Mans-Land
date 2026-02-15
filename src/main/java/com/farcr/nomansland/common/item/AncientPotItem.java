package com.farcr.nomansland.common.item;

import com.farcr.nomansland.common.block.pots.PotSize;
import com.farcr.nomansland.common.block.pots.PotTrait;
import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.entity.LivingPot;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class AncientPotItem extends BlockItem {
    private final PotSize size;

    public AncientPotItem(PotSize size, Block block, Properties properties) {
        super(block, properties);
        this.size = size;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();

        Registry<PotVariant> variantRegistry = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY);
        List<Holder.Reference<PotVariant>> variants = variantRegistry.holders().filter(variant -> variant.value().size() == size).toList();
        PotVariant variant = variantRegistry.getOptional(stack.get(NMLDataComponents.POT_VARIANT.get())).orElse(variants.get(level.getRandom().nextInt(variants.size())).value());

        if (variant.traits().contains(PotTrait.LIVING)) {
            BlockPos pos = context.getClickedPos();
            LivingPot livingPot = new LivingPot(level, pos.getX(), pos.getY(), pos.getZ(), variant.size() == PotSize.SMALL ? NMLBlocks.ANCIENT_POT.get().defaultBlockState() : NMLBlocks.LARGE_ANCIENT_POT.get().defaultBlockState());
            level.addFreshEntity(livingPot);
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
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
                    String name = trait.getSerializedName().replace("_", " ");
                    tooltipComponents.add(Component.literal(Character.toUpperCase(name.charAt(0)) + name.substring(1)).withStyle(ChatFormatting.DARK_GREEN));
                });
            } else tooltipComponents.add(Component.literal(variantLocation.toString()));
        }
    }

    // TODO: variant item model rendering

    // TODO: variant hitboxes
}
