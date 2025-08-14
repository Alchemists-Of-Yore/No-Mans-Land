package com.farcr.nomansland.common.world.generation;

import com.farcr.nomansland.common.registry.worldgen.NMLNoises;
import com.farcr.nomansland.common.world.densityfunction.*;
import com.farcr.nomansland.common.world.densityfunction.modification.DensityFunctionModifications;
import com.farcr.nomansland.common.world.densityfunction.modification.NoiseRouterParameter;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.*;

public class NMLDensityModifications {
    public static void register() {
        DensityFunctionModifications.addNoiseRouterParameterModifier(BuiltinDimensionTypes.OVERWORLD, NoiseRouterParameter.FINAL_DENSITY, (originalDensityFunction, noiseParameters, densityFunctions) -> {
            DensityFunction horizontalRiverDistance = DensityFunctions.interpolated(DensityFunctions.flatCache(
                    new CaveRiverDistanceDensityFunction(true, null)
            ));
            DensityFunction verticalRiverDistance = DensityFunctions.cache2d(
                    new CaveRiverDistanceDensityFunction(false, null)
            );
            DensityFunction caveRiverRadius = DensityFunctions.interpolated(DensityFunctions.flatCache(
                    NMLDensityUtils.mapRangeClamped(0, 1, 11, 30,
                            DensityFunctions.noise(noiseParameters.getOrThrow(NMLNoises.CAVE_RIVER_RADIUS), 3, 1).abs().square() // bias it towards smaller values
                    )
            ));
            DensityFunction caveRiverDensity = new CaveRiverDensityFunction(horizontalRiverDistance, verticalRiverDistance, caveRiverRadius, null);
            DensityFunction caveRiverShoreDensity = DensityFunctions.interpolated(
                    new CaveRiverShoreDensityFunction(horizontalRiverDistance, verticalRiverDistance, caveRiverRadius, null)
            );

            // add noise
            DensityFunction caveRiverNoise = DensityFunctions.interpolated(
                    NMLDensityUtils.mapRange(-1, 1, -0.1, 0.02,
                            DensityFunctions.noise(noiseParameters.getOrThrow(Noises.ICE), 0.5)
                    )
            );
            caveRiverDensity = DensityFunctions.add(
                    caveRiverDensity,
                    caveRiverNoise
            );
            caveRiverShoreDensity = DensityFunctions.add(
                    caveRiverShoreDensity,
                    DensityFunctions.mul(caveRiverNoise, DensityFunctions.constant(2.5))
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
            return NMLDensityUtils.smoothMin(0.04,
                    caveRiverDensity,
                    NMLDensityUtils.smoothMax(0.08, originalDensityFunction,
                            caveRiverShoreDensity
                    )
            );
        });
    }

    private static DensityFunction getFunction(HolderGetter<DensityFunction> densityFunctions, ResourceKey<DensityFunction> key) {
        return new DensityFunctions.HolderHolder(densityFunctions.getOrThrow(key));
    }
}
