package com.farcr.nomansland.common.world.densityfunction.modification;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.function.Function;

public enum NoiseRouterParameter {
    BARRIER_NOISE(NoiseRouter::barrierNoise),
    FLUID_LEVEL_FLOODEDNESS_NOISE(NoiseRouter::fluidLevelFloodednessNoise),
    FLUID_LEVEL_SPREAD_NOISE(NoiseRouter::fluidLevelSpreadNoise),
    LAVA_NOISE(NoiseRouter::lavaNoise),
    TEMPERATURE(NoiseRouter::temperature),
    VEGETATION(NoiseRouter::vegetation),
    CONTINENTS(NoiseRouter::continents),
    EROSION(NoiseRouter::erosion),
    DEPTH(NoiseRouter::depth),
    RIDGES(NoiseRouter::ridges),
    INITIAL_DENSITY_WITHOUT_JAGGEDNESS(NoiseRouter::initialDensityWithoutJaggedness),
    FINAL_DENSITY(NoiseRouter::finalDensity),
    VEIN_TOGGLE(NoiseRouter::veinToggle),
    VEIN_RIDGED(NoiseRouter::veinRidged),
    VEIN_GAP(NoiseRouter::veinGap);

    private final Function<NoiseRouter, DensityFunction> getter;

    NoiseRouterParameter(Function<NoiseRouter, DensityFunction> getter) {
        this.getter = getter;
    }

    public DensityFunction maybeModify(NoiseRouter noiseRouter, DensityFunctionModifications.NoiseRouterModifications modifications, HolderGetter<NormalNoise.NoiseParameters> noiseRegistry) {
        DensityFunction originalFunction = this.getter.apply(noiseRouter);
        if (!modifications.modifiers.containsKey(this)) return originalFunction;
        return modifications.modifiers.get(this).visit(originalFunction, noiseRegistry);
    }
}
