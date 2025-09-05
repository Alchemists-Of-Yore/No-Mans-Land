package com.farcr.nomansland.common.integration;

import com.farcr.nomansland.common.definitions.ItemDefinition;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.teamabnormals.boatload.common.item.FurnaceBoatItem;
import com.teamabnormals.boatload.common.item.LargeBoatItem;
import com.teamabnormals.boatload.core.registry.BoatloadItems;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class BoatloadIntegration {

    public static final ItemDefinition<FurnaceBoatItem> MAPLE_FURNACE_BOAT = NMLItems.register("maple_furnace_boat",
            () -> new FurnaceBoatItem(BoatloadBoatTypes.MAPLE), true);

    public static final ItemDefinition<LargeBoatItem> LARGE_MAPLE_BOAT = NMLItems.register("large_maple_boat",
            () -> new LargeBoatItem(BoatloadBoatTypes.MAPLE));

    public static final ItemDefinition<FurnaceBoatItem> PINE_FURNACE_BOAT = NMLItems.register("pine_furnace_boat",
            () -> new FurnaceBoatItem(BoatloadBoatTypes.PINE), true);

    public static final ItemDefinition<LargeBoatItem> LARGE_PINE_BOAT = NMLItems.register("large_pine_boat",
            () -> new LargeBoatItem(BoatloadBoatTypes.PINE));

    public static final ItemDefinition<FurnaceBoatItem> WALNUT_FURNACE_BOAT = NMLItems.register("walnut_furnace_boat",
            () -> new FurnaceBoatItem(BoatloadBoatTypes.WALNUT), true);

    public static final ItemDefinition<LargeBoatItem> LARGE_WALNUT_BOAT = NMLItems.register("large_walnut_boat",
            () -> new LargeBoatItem(BoatloadBoatTypes.WALNUT));

    public static final ItemDefinition<FurnaceBoatItem> WILLOW_FURNACE_BOAT = NMLItems.register("willow_furnace_boat",
            () -> new FurnaceBoatItem(BoatloadBoatTypes.WILLOW), true);

    public static final ItemDefinition<LargeBoatItem> LARGE_WILLOW_BOAT = NMLItems.register("large_willow_boat",
            () -> new LargeBoatItem(BoatloadBoatTypes.WALNUT));

    public static void register() {
    }
}
