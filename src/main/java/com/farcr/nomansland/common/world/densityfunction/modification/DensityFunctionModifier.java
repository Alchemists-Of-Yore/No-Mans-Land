package com.farcr.nomansland.common.world.densityfunction.modification;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface DensityFunctionModifier {
    DensityFunction apply(DensityFunction densityFunction, HolderGetter<NormalNoise.NoiseParameters> noiseParameterRegistry, HolderGetter<DensityFunction> densityFunctionRegistry);

    default DensityFunctionModifier combine(DensityFunctionModifier other) {
        return (function, noiseParameterRegistry, densityFunctionRegistry) ->
                this.apply(
                        other.apply(function, noiseParameterRegistry, densityFunctionRegistry),
                        noiseParameterRegistry, densityFunctionRegistry
                );
    }

    default DensityFunction visit(DensityFunction densityFunction, HolderGetter<NormalNoise.NoiseParameters> noiseParameterRegistry, HolderGetter<DensityFunction> densityFunctionRegistry) {
        if (densityFunction instanceof DensityFunctions.Marker marker) {
            return new DensityFunctions.Marker(marker.type(), this.visit(marker.wrapped(), noiseParameterRegistry, densityFunctionRegistry));
        }
        return this.apply(densityFunction, noiseParameterRegistry, densityFunctionRegistry);
    }
}
