package com.farcr.nomansland.common.world.densityfunction.modification;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public interface DensityFunctionModifier {
    DensityFunction apply(DensityFunction densityFunction, HolderGetter<NormalNoise.NoiseParameters> noiseParameters);

    default DensityFunctionModifier combine(DensityFunctionModifier other) {
        return (function, params) -> this.apply(other.apply(function, params), params);
    }

    default DensityFunction visit(DensityFunction densityFunction, HolderGetter<NormalNoise.NoiseParameters> noiseParameters) {
        if (densityFunction instanceof DensityFunctions.Marker marker) {
            return new DensityFunctions.Marker(marker.type(), this.visit(marker.wrapped(), noiseParameters));
        }
        return this.apply(densityFunction, noiseParameters);
    }
}
