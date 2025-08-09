package com.farcr.nomansland.common.world.generation;

import com.farcr.nomansland.common.world.densityfunction.FunkyTestDensityFunction;
import com.farcr.nomansland.common.world.densityfunction.modification.DensityFunctionModifications;
import com.farcr.nomansland.common.world.densityfunction.modification.NoiseRouterParameter;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouterData;

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
    }
}
