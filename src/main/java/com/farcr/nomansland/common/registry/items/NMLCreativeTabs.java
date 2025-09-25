package com.farcr.nomansland.common.registry.items;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.definitions.ItemDefinition;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.google.common.collect.Sets;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class NMLCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NoMansLand.MODID);

    private static final Sets.SetView<ItemDefinition<?>> CREATIVE_TAB_ITEMS = Sets.union(NMLItems.CREATIVE_TAB_ITEMS, NMLBlocks.CREATIVE_TAB_ITEMS);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NO_MANS_TAB = CREATIVE_TABS.register(NoMansLand.MODID,
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nomansland"))
                    .icon(NMLItems.NO_MANS_GLOBE::stack)
                    .displayItems(CREATIVE_TAB_ITEMS)
                    .build());
}
