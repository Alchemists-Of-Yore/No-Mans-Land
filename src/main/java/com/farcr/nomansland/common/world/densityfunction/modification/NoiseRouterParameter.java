package com.farcr.nomansland.common.world.densityfunction.modification;

import com.farcr.nomansland.common.mixin.NoiseRouterAccessor;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum NoiseRouterParameter {
    BARRIER_NOISE(NoiseRouter::barrierNoise, NoiseRouterAccessor::nml$setBarrierNoise),
    FLUID_LEVEL_FLOODEDNESS_NOISE(NoiseRouter::fluidLevelFloodednessNoise, NoiseRouterAccessor::nml$setFluidLevelFloodednessNoise),
    FLUID_LEVEL_SPREAD_NOISE(NoiseRouter::fluidLevelSpreadNoise, NoiseRouterAccessor::nml$setFluidLevelSpreadNoise),
    LAVA_NOISE(NoiseRouter::lavaNoise, NoiseRouterAccessor::nml$setLavaNoise),
    TEMPERATURE(NoiseRouter::temperature, NoiseRouterAccessor::nml$setTemperature),
    VEGETATION(NoiseRouter::vegetation, NoiseRouterAccessor::nml$setVegetation),
    CONTINENTS(NoiseRouter::continents, NoiseRouterAccessor::nml$setContinents),
    EROSION(NoiseRouter::erosion, NoiseRouterAccessor::nml$setErosion),
    DEPTH(NoiseRouter::depth, NoiseRouterAccessor::nml$setDepth),
    RIDGES(NoiseRouter::ridges, NoiseRouterAccessor::nml$setRidges),
    INITIAL_DENSITY_WITHOUT_JAGGEDNESS(NoiseRouter::initialDensityWithoutJaggedness, NoiseRouterAccessor::nml$setInitialDensityWithoutJaggedness),
    FINAL_DENSITY(NoiseRouter::finalDensity, NoiseRouterAccessor::nml$setFinalDensity),
    VEIN_TOGGLE(NoiseRouter::veinToggle, NoiseRouterAccessor::nml$setVeinToggle),
    VEIN_RIDGED(NoiseRouter::veinRidged, NoiseRouterAccessor::nml$setVeinRidged),
    VEIN_GAP(NoiseRouter::veinGap, NoiseRouterAccessor::nml$setVeinGap);

    private final Function<NoiseRouter, DensityFunction> getter;
    private final BiConsumer<NoiseRouterAccessor, DensityFunction> setter;

    NoiseRouterParameter(Function<NoiseRouter, DensityFunction> getter, BiConsumer<NoiseRouterAccessor, DensityFunction> setter) {
        this.getter = getter;
        this.setter = setter;
    }

    public void modify(NoiseRouter router, DensityFunctionModifier modifier) {
        DensityFunction modifiedFunction = modifier.visit(this.getter.apply(router));
        this.setter.accept(((NoiseRouterAccessor)(Object)router), modifiedFunction);
        //NoMansLand.LOGGER.info("[\"{}\"] : {}", this.name(), modifiedFunction);
    }
}
