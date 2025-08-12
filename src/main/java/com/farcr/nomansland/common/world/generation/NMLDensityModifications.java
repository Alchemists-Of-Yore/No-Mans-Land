package com.farcr.nomansland.common.world.generation;

import com.farcr.nomansland.common.registry.worldgen.NMLNoises;
import com.farcr.nomansland.common.world.densityfunction.CaveRiverDistanceDensityFunction;
import com.farcr.nomansland.common.world.densityfunction.NMLDensityUtils;
import com.farcr.nomansland.common.world.densityfunction.modification.DensityFunctionModifications;
import com.farcr.nomansland.common.world.densityfunction.modification.NoiseRouterParameter;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.*;
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

        DensityFunctionModifications.addNoiseRouterParameterModifier(BuiltinDimensionTypes.OVERWORLD, NoiseRouterParameter.FINAL_DENSITY, (originalDensityFunction, noiseParameters, densityFunctions) -> {
            DensityFunction caveRiverDensity = new CaveRiverDistanceDensityFunction(
                    DensityFunctions.interpolated(
                            DensityFunctions.cache2d(
                                    NMLDensityUtils.mapRange(-1, 1, 8, 28,
                                            DensityFunctions.noise(noiseParameters.getOrThrow(NMLNoises.CAVE_RIVER_RADIUS))
                                    )
                            )
                    ), null
            );
            // add noise
            caveRiverDensity = DensityFunctions.add(
                    caveRiverDensity,
                    DensityFunctions.interpolated(
                            NMLDensityUtils.mapRange(-1, 1, -0.1, 0.1,
                                DensityFunctions.noise(noiseParameters.getOrThrow(Noises.ICE), 0.5)
                            )
                    )
            );
            // add speleothems
            caveRiverDensity = DensityFunctions.add(
                    caveRiverDensity,
                    DensityFunctions.interpolated(
                            NMLDensityUtils.smoothMax(0.1,
                                    DensityFunctions.constant(0),
                                    DensityFunctions.add(
                                            DensityFunctions.constant(-0.04),
                                            getFunction(densityFunctions, NoiseRouterData.PILLARS)
                                    )
                            )
                    )
            );
            return NMLDensityUtils.smoothMin(0.03, originalDensityFunction, caveRiverDensity);
        });
    }

    private static DensityFunction getFunction(HolderGetter<DensityFunction> densityFunctions, ResourceKey<DensityFunction> key) {
        return new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(key));
    }
}
