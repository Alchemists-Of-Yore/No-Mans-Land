package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.densityfunction.FunkyTestDensityFunction;
import com.farcr.nomansland.common.world.densityfunction.RangeSelectDensityFunction;
import com.farcr.nomansland.common.world.densityfunction.SmoothMixDensityFunction;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLDensityFunctions {
    public static final DeferredRegister<MapCodec<? extends DensityFunction>> DENSITY_FUNCTIONS = DeferredRegister.create(Registries.DENSITY_FUNCTION_TYPE, NoMansLand.MODID);

    public static final Supplier<MapCodec<FunkyTestDensityFunction>> FUNKY_TEST =
            DENSITY_FUNCTIONS.register("funky_test", FunkyTestDensityFunction.CODEC::codec);
    public static final Supplier<MapCodec<RangeSelectDensityFunction>> RANGE_SELECT =
            DENSITY_FUNCTIONS.register("range_select", RangeSelectDensityFunction.CODEC::codec);
    public static final Supplier<MapCodec<SmoothMixDensityFunction>> SMOOTH_MIX =
            DENSITY_FUNCTIONS.register("smooth_mix", SmoothMixDensityFunction.CODEC::codec);
}
