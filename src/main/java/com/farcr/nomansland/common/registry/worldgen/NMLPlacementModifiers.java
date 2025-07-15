package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.feature.*;
import com.farcr.nomansland.common.world.feature.placementmodifiers.DensityFunctionBasedCountPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLPlacementModifiers {

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, NoMansLand.MODID);

    public static final Supplier<PlacementModifierType<DensityFunctionBasedCountPlacement>> DENSITY_FUNCTION_BASED_COUNT =
            PLACEMENT_MODIFIER_TYPES.register("density_function_based_count", () -> () -> DensityFunctionBasedCountPlacement.CODEC);
}
