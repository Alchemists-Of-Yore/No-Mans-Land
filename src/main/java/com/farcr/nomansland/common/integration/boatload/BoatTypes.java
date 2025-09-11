package com.farcr.nomansland.common.integration.boatload;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.teamabnormals.boatload.core.api.BoatloadBoatType;

public class BoatTypes {


    public static final BoatloadBoatType MAPLE = BoatloadBoatType.register(BoatloadBoatType.create(NoMansLand.location("maple"),
            NMLBlocks.MAPLE.planks()::item,
            NMLItems.MAPLE_BOAT::item,
            NMLItems.MAPLE_CHEST_BOAT::item,
            BoatloadIntegration.MAPLE_FURNACE_BOAT::item,
            BoatloadIntegration.LARGE_MAPLE_BOAT::item
    ));

    public static final BoatloadBoatType PINE = BoatloadBoatType.register(BoatloadBoatType.create(NoMansLand.location("pine"),
            NMLBlocks.MAPLE.planks()::item,
            NMLItems.MAPLE_BOAT::item,
            NMLItems.MAPLE_CHEST_BOAT::item,
            BoatloadIntegration.PINE_FURNACE_BOAT::item,
            BoatloadIntegration.LARGE_PINE_BOAT::item
    ));

    public static final BoatloadBoatType WALNUT = BoatloadBoatType.register(BoatloadBoatType.create(NoMansLand.location("walnut"),
            NMLBlocks.MAPLE.planks()::item,
            NMLItems.MAPLE_BOAT::item,
            NMLItems.MAPLE_CHEST_BOAT::item,
            BoatloadIntegration.WALNUT_FURNACE_BOAT::item,
            BoatloadIntegration.LARGE_WALNUT_BOAT::item
    ));

    public static final BoatloadBoatType WILLOW = BoatloadBoatType.register(BoatloadBoatType.create(NoMansLand.location("willow"),
            NMLBlocks.MAPLE.planks()::item,
            NMLItems.MAPLE_BOAT::item,
            NMLItems.MAPLE_CHEST_BOAT::item,
            BoatloadIntegration.WILLOW_FURNACE_BOAT::item,
            BoatloadIntegration.LARGE_WILLOW_BOAT::item
    ));
}
