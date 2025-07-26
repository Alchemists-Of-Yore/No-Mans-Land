package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.densityfunction.RangeSelectionDensityFunction;
import com.farcr.nomansland.common.world.feature.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLDensityFunctions {
    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTIONS = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, NoMansLand.MODID);

    public static final Supplier<MapCodec<RangeSelectionDensityFunction>> RANGE_SELECTION =
            DENSITY_FUNCTIONS.register("range_selection", RangeSelectionDensityFunction.CODEC::codec);
}
