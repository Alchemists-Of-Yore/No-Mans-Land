package com.farcr.nomansland.common.world.generation;

import com.farcr.nomansland.common.registry.worldgen.NMLNoises;
import com.farcr.nomansland.common.world.densityfunction.CaveRiverDensityFunction;
import com.farcr.nomansland.common.world.densityfunction.CaveRiverDistanceDensityFunction;
import com.farcr.nomansland.common.world.densityfunction.CaveRiverTestDensityFunction;
import com.farcr.nomansland.common.world.densityfunction.NMLDensityUtils;
import com.farcr.nomansland.common.world.densityfunction.modification.DensityFunctionModifications;
import com.farcr.nomansland.common.world.densityfunction.modification.NoiseRouterParameter;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.*;

public class NMLDensityModifications {
    public static void register() {
        DensityFunctionModifications.addNoiseRouterParameterModifier(BuiltinDimensionTypes.OVERWORLD, NoiseRouterParameter.FINAL_DENSITY, (originalDensityFunction, noiseParameters, densityFunctions) -> {
            DensityFunction caveRiverDensity = new CaveRiverDensityFunction(
                    DensityFunctions.interpolated(DensityFunctions.flatCache(
                            new CaveRiverDistanceDensityFunction(true, null)
                    )),
                    DensityFunctions.cache2d(
                            new CaveRiverDistanceDensityFunction(false, null)
                    ),
                    DensityFunctions.interpolated(DensityFunctions.flatCache(
                            NMLDensityUtils.mapRangeClamped(0, 1, 11, 30,
                                    DensityFunctions.noise(noiseParameters.getOrThrow(NMLNoises.CAVE_RIVER_RADIUS), 3, 1).abs().square() // bias it towards smaller values
                            )
                    )), null
            );


            // add noise
            caveRiverDensity = DensityFunctions.add(
                    caveRiverDensity,
                    DensityFunctions.interpolated(
                            NMLDensityUtils.mapRange(-1, 1, -0.1, 0.02,
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
                                            DensityFunctions.constant(-0.03),
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
