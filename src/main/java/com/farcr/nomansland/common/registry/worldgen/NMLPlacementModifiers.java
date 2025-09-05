package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.feature.placementmodifier.DensityFunctionBasedCountPlacement;
import com.farcr.nomansland.common.world.feature.placementmodifier.DensityFunctionBasedProbabilityPlacement;
import com.farcr.nomansland.common.world.feature.placementmodifier.HeightFilterPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLPlacementModifiers {

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, NoMansLand.MODID);

    public static final Supplier<PlacementModifierType<DensityFunctionBasedCountPlacement>> DENSITY_FUNCTION_BASED_COUNT =
            PLACEMENT_MODIFIER_TYPES.register("density_function_based_count", () -> () -> DensityFunctionBasedCountPlacement.CODEC);
    public static final Supplier<PlacementModifierType<DensityFunctionBasedProbabilityPlacement>> DENSITY_FUNCTION_BASED_PROBABILITY =
            PLACEMENT_MODIFIER_TYPES.register("density_function_based_probability", () -> () -> DensityFunctionBasedProbabilityPlacement.CODEC);
    public static final Supplier<PlacementModifierType<HeightFilterPlacement>> HEIGHT_FILTER =
            PLACEMENT_MODIFIER_TYPES.register("height_filter", () -> () -> HeightFilterPlacement.CODEC);
}
