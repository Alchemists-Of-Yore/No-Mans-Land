package com.farcr.nomansland.common.world.generation;

import com.farcr.nomansland.common.registry.worldgen.NMLNoises;
import com.farcr.nomansland.common.world.densityfunction.CaveRiverDistanceDensityFunction;
import com.farcr.nomansland.common.world.densityfunction.NMLDensityUtils;
import com.farcr.nomansland.common.world.densityfunction.modification.DensityFunctionModifications;
import com.farcr.nomansland.common.world.densityfunction.modification.NoiseRouterParameter;
import net.minecraft.core.Holder;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class NMLDensityModifications {
    public static void register() {
//        // modify a specific noise .json
//        DensityFunctionModifications.addModifier(NoiseRouterData.OFFSET, originalDensityFunction -> {
//            return new FunkyTestDensityFunction(originalDensityFunction);
//        });
//        // modify a noise router parameter
//        DensityFunctionModifications.addNoiseRouterParameterModifier(BuiltinDimensionTypes.OVERWORLD, NoiseRouterParameter.EROSION, originalDensityFunction -> {
//            return DensityFunctions.add(DensityFunctions.constant(0.5), originalDensityFunction)
//        });

//        DensityFunctionModifications.addModifier(NoiseRouterData.NOODLE, originalDensityFunction ->
//                NMLDensityUtils.smoothMin(originalDensityFunction, new CaveRiverTestDensityFunction(), 0.01)
//        );

        DensityFunctionModifications.addNoiseRouterParameterModifier(BuiltinDimensionTypes.OVERWORLD, NoiseRouterParameter.FINAL_DENSITY, (originalDensityFunction, noiseParameters) -> {
            DensityFunction caveRiverDensity = new CaveRiverDistanceDensityFunction(
                    DensityFunctions.interpolated(
                            DensityFunctions.cache2d(
                                    NMLDensityUtils.mapRange(-1, 1, 8, 28,
                                            DensityFunctions.noise(noiseParameters.getOrThrow(NMLNoises.CAVE_RIVER_RADIUS))
                                    )
                            )
                    ), null
            );
//            DensityFunction caveRiverDensity = NMLDensityUtils.mapRangeClamped(
//                    DensityFunctions.add(caveRiverDistance, DensityFunctions.constant(-15.0)),
//                    -10, 10, -1, 1
//            );
            caveRiverDensity = DensityFunctions.add(
                    caveRiverDensity,
                    DensityFunctions.interpolated(
                            NMLDensityUtils.mapRange(-1, 1, -0.1, 0.1,
                                DensityFunctions.noise(noiseParameters.getOrThrow(Noises.ICE), 0.5)
                            )
                    )
            );
            return NMLDensityUtils.smoothMin(0.03, originalDensityFunction, caveRiverDensity);
        });


        DensityFunctionModifications.addNoiseRouterParameterModifier(BuiltinDimensionTypes.OVERWORLD, NoiseRouterParameter.FLUID_LEVEL_FLOODEDNESS_NOISE, (originalDensityFunction, noiseParameters) -> {
            return DensityFunctions.constant(0);
        });
        DensityFunctionModifications.addNoiseRouterParameterModifier(BuiltinDimensionTypes.OVERWORLD, NoiseRouterParameter.FLUID_LEVEL_SPREAD_NOISE, (originalDensityFunction, noiseParameters) -> {
            return DensityFunctions.constant(0);
        });
    }
}
