package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.mixinextensions.NoiseRouterExtension;
import com.farcr.nomansland.common.world.watershed.Watershed;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseRouter.class)
public class NoiseRouterMixin implements NoiseRouterExtension {
    @Unique
    public DensityFunction nml$watershedProbabilityNoise;
    @Unique
    public DensityFunction nml$watershedSourceHeightNoise;
    @Unique
    public DensityFunction nml$watershedDrainHeightNoise;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nml$init(
            DensityFunction barrierNoise,
            DensityFunction fluidLevelFloodednessNoise,
            DensityFunction fluidLevelSpreadNoise,
            DensityFunction lavaNoise,
            DensityFunction temperature,
            DensityFunction vegetation,
            DensityFunction continents,
            DensityFunction erosion,
            DensityFunction depth,
            DensityFunction ridges,
            DensityFunction initialDensityWithoutJaggedness,
            DensityFunction finalDensity,
            DensityFunction veinToggle,
            DensityFunction veinRidged,
            DensityFunction veinGap,
            CallbackInfo ci) {
        this.nml$watershedProbabilityNoise = Watershed.WATERSHED_PROBABILITY.get();
        this.nml$watershedSourceHeightNoise = Watershed.WATERSHED_SOURCE_HEIGHT.get();
        this.nml$watershedDrainHeightNoise = Watershed.WATERSHED_DRAIN_HEIGHT.get();
    }

    @Inject(method = "mapAll", at = @At("TAIL"))
    private void nml$mapAll(DensityFunction.Visitor visitor, CallbackInfoReturnable<NoiseRouter> cir) {
        this.nml$watershedProbabilityNoise.mapAll(visitor);
        this.nml$watershedSourceHeightNoise.mapAll(visitor);
        this.nml$watershedDrainHeightNoise.mapAll(visitor);
    }

    @Override @Unique
    public DensityFunction nml$watershedProbabilityNoise() {
        return nml$watershedProbabilityNoise;
    }

    @Override @Unique
    public DensityFunction nml$watershedSourceHeightNoise() {
        return nml$watershedSourceHeightNoise;
    }

    @Override @Unique
    public DensityFunction nml$watershedDrainHeightNoise() {
        return nml$watershedDrainHeightNoise;
    }
}
