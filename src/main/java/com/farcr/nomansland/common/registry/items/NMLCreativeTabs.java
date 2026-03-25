package com.farcr.nomansland.common.registry.items;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.pots.PotSize;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Comparator;

@SuppressWarnings("unused")
public class NMLCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NoMansLand.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NO_MANS_TAB = CREATIVE_TABS.register(NoMansLand.MODID,
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nomansland"))
                    .icon(NMLItems.NO_MANS_GLOBE::stack)
                    .displayItems(NMLItems.CREATIVE_TAB_ITEMS)
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ANCIENT_POTS = CREATIVE_TABS.register(NoMansLand.MODID + "_ancient_pots",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nomansland.ancient_pots"))
                    .icon(() -> {
                        ItemStack stack = NMLItems.LARGE_ANCIENT_POT.stack();
                        stack.set(NMLDataComponents.POT_VARIANT, NoMansLand.location("ancient_pot_small_1"));

                        return stack;
                    })
                    .displayItems((parameters, output) -> {
                        parameters.holders().lookup(NMLRegistries.POT_VARIANT_KEY).ifPresent((lookup) -> {
                            RegistryOps<Tag> registryops = parameters.holders().createSerializationContext(NbtOps.INSTANCE);
                            lookup.listElements().sorted(Comparator.comparing(Holder::value, Comparator.comparingInt((pot) -> pot.size().ordinal()))).forEach((variant) -> {
                                ItemStack itemstack = variant.value().size() == PotSize.SMALL ? NMLItems.ANCIENT_POT.stack() : NMLItems.LARGE_ANCIENT_POT.stack();
                                itemstack.set(NMLDataComponents.POT_VARIANT, variant.key.location());
                                output.accept(itemstack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                            });
                        });

                        // TODO: when ready
//                        output.accept(NMLItems.ANCIENT_POT_DEBUG_ITEM);
                    })
                    .build());
}
