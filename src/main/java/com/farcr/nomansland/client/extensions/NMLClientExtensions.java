package com.farcr.nomansland.client.extensions;

import com.farcr.nomansland.*;
import com.farcr.nomansland.client.*;
import com.farcr.nomansland.common.registry.*;
import com.farcr.nomansland.common.registry.items.*;
import net.minecraft.resources.*;
import net.neoforged.neoforge.client.extensions.common.*;

public class NMLClientExtensions {

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL_RESIN_OIL = NoMansLand.location("block/fluid/resin_oil");
            private static final ResourceLocation FLOWING_RESIN_OIL = NoMansLand.location("block/fluid/flowing_resin_oil");

            public ResourceLocation getStillTexture() {
                return STILL_RESIN_OIL;
            }

            public ResourceLocation getFlowingTexture() {
                return FLOWING_RESIN_OIL;
            }
        }, NMLFluids.RESIN_OIL_TYPE.get());

        event.registerItem(new LodestoneArmorClientItemExtensions(()->NMLArmorModels.ANCIENT_BRONZE_MASK), NMLItems.ANCIENT_BRONZE_MASK.get());
        event.registerItem(new LodestoneArmorClientItemExtensions(()->NMLArmorModels.TORTOISE_SHELL), NMLItems.TORTOISE_SHELL.get());
    }
}
