package com.farcr.nomansland.common.world.densityfunction.modification;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.mixin.NoiseRouterAccessModifier;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum NoiseRouterParameter {
    BARRIER_NOISE(NoiseRouter::barrierNoise, NoiseRouterAccessModifier::nml$setBarrierNoise),
    FLUID_LEVEL_FLOODEDNESS_NOISE(NoiseRouter::fluidLevelFloodednessNoise, NoiseRouterAccessModifier::nml$setFluidLevelFloodednessNoise),
    FLUID_LEVEL_SPREAD_NOISE(NoiseRouter::fluidLevelSpreadNoise, NoiseRouterAccessModifier::nml$setFluidLevelSpreadNoise),
    LAVA_NOISE(NoiseRouter::lavaNoise, NoiseRouterAccessModifier::nml$setLavaNoise),
    TEMPERATURE(NoiseRouter::temperature, NoiseRouterAccessModifier::nml$setTemperature),
    VEGETATION(NoiseRouter::vegetation, NoiseRouterAccessModifier::nml$setVegetation),
    CONTINENTS(NoiseRouter::continents, NoiseRouterAccessModifier::nml$setContinents),
    EROSION(NoiseRouter::erosion, NoiseRouterAccessModifier::nml$setErosion),
    DEPTH(NoiseRouter::depth, NoiseRouterAccessModifier::nml$setDepth),
    RIDGES(NoiseRouter::ridges, NoiseRouterAccessModifier::nml$setRidges),
    INITIAL_DENSITY_WITHOUT_JAGGEDNESS(NoiseRouter::initialDensityWithoutJaggedness, NoiseRouterAccessModifier::nml$setInitialDensityWithoutJaggedness),
    FINAL_DENSITY(NoiseRouter::finalDensity, NoiseRouterAccessModifier::nml$setFinalDensity),
    VEIN_TOGGLE(NoiseRouter::veinToggle, NoiseRouterAccessModifier::nml$setVeinToggle),
    VEIN_RIDGED(NoiseRouter::veinRidged, NoiseRouterAccessModifier::nml$setVeinRidged),
    VEIN_GAP(NoiseRouter::veinGap, NoiseRouterAccessModifier::nml$setVeinGap);

    private final Function<NoiseRouter, DensityFunction> getter;
    private final BiConsumer<NoiseRouterAccessModifier, DensityFunction> setter;

    NoiseRouterParameter(Function<NoiseRouter, DensityFunction> getter, BiConsumer<NoiseRouterAccessModifier, DensityFunction> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public void modify(NoiseRouter router, DensityFunctionModifier modifier) {
        DensityFunction modifiedFunction = modifier.visit(this.getter.apply(router));
        this.setter.accept(((NoiseRouterAccessModifier)(Object)router), modifiedFunction);
        //NoMansLand.LOGGER.info("[\"{}\"] : {}", this.name(), modifiedFunction);
    }
}
