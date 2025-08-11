package com.farcr.nomansland.common.world.generation;

import com.farcr.nomansland.common.world.densityfunction.CaveRiverDistanceDensityFunction;
import com.farcr.nomansland.common.world.densityfunction.NMLDensityUtils;
import com.farcr.nomansland.common.world.densityfunction.modification.DensityFunctionModifications;
import com.farcr.nomansland.common.world.densityfunction.modification.NoiseRouterParameter;
import net.minecraft.core.Holder;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
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

        DensityFunctionModifications.addNoiseRouterParameterModifier(BuiltinDimensionTypes.OVERWORLD, NoiseRouterParameter.FINAL_DENSITY, originalDensityFunction -> {
            DensityFunction caveRiverDistance = new CaveRiverDistanceDensityFunction(
                    DensityFunctions.interpolated(DensityFunctions.flatCache(
                            DensityFunctions.noise(Holder.direct(new NormalNoise.NoiseParameters(-6, 1, 0, 1, 0.2)))
                    )),
                    DensityFunctions.interpolated(DensityFunctions.flatCache(
                            DensityFunctions.noise(Holder.direct(new NormalNoise.NoiseParameters(-6, 1, 0, 1, 0.2)))
                    )),
                    null
            );
            DensityFunction caveRiverDensity = NMLDensityUtils.mapRangeClamped(
                    DensityFunctions.add(caveRiverDistance, DensityFunctions.constant(-5.0)),
                    -10, 10, -1, 1
            );

            return NMLDensityUtils.smoothMin(originalDensityFunction, caveRiverDensity, 0.2);
        });
    }
}
