package com.farcr.nomansland.common.mixin;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NoiseRouter.class)
public interface NoiseRouterAccessModifier {
    @Final @Mutable @Accessor("barrierNoise")
    void nml$setBarrierNoise(DensityFunction densityFunction);
    @Final @Mutable @Accessor("fluidLevelFloodednessNoise")
    void nml$setFluidLevelFloodednessNoise(DensityFunction densityFunction);
    @Final @Mutable @Accessor("fluidLevelSpreadNoise")
    void nml$setFluidLevelSpreadNoise(DensityFunction densityFunction);
    @Final @Mutable @Accessor("lavaNoise")
    void nml$setLavaNoise(DensityFunction densityFunction);
    @Final @Mutable @Accessor("temperature")
    void nml$setTemperature(DensityFunction densityFunction);
    @Final @Mutable @Accessor("vegetation")
    void nml$setVegetation(DensityFunction densityFunction);
    @Final @Mutable @Accessor("continents")
    void nml$setContinents(DensityFunction densityFunction);
    @Final @Mutable @Accessor("erosion")
    void nml$setErosion(DensityFunction densityFunction);
    @Final @Mutable @Accessor("depth")
    void nml$setDepth(DensityFunction densityFunction);
    @Final @Mutable @Accessor("ridges")
    void nml$setRidges(DensityFunction densityFunction);
    @Final @Mutable @Accessor("initialDensityWithoutJaggedness")
    void nml$setInitialDensityWithoutJaggedness(DensityFunction densityFunction);
    @Final @Mutable @Accessor("finalDensity")
    void nml$setFinalDensity(DensityFunction densityFunction);
    @Final @Mutable @Accessor("veinToggle")
    void nml$setVeinToggle(DensityFunction densityFunction);
    @Final @Mutable @Accessor("veinRidged")
    void nml$setVeinRidged(DensityFunction densityFunction);
    @Final @Mutable @Accessor("veinGap")
    void nml$setVeinGap(DensityFunction densityFunction);
}
