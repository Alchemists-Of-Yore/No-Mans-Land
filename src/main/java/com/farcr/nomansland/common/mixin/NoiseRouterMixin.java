package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.mixinextensions.WatershedNoiseRouterHolder;
import com.farcr.nomansland.common.world.watershed.WatershedNoiseRouter;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseRouter.class)
public class NoiseRouterMixin implements WatershedNoiseRouterHolder {
    @Unique
    private WatershedNoiseRouter nml$watershedNoiseRouter;

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
        this.nml$watershedNoiseRouter = new WatershedNoiseRouter();
    }

    @WrapMethod(method = "mapAll")
    private NoiseRouter nml$mapAll(DensityFunction.Visitor visitor, Operation<NoiseRouter> original) {
        NoiseRouter mappedRouter = original.call(visitor);
        ((NoiseRouterMixin)(Object) mappedRouter).nml$watershedNoiseRouter = this.nml$watershedNoiseRouter.mapAll(visitor);
        return mappedRouter;
    }

    @Override
    public WatershedNoiseRouter nml$watershedNoiseRouter() {
        return this.nml$watershedNoiseRouter;
    }
}
