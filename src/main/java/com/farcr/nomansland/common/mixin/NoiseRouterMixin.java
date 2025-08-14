package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.mixinextensions.WatershedNoiseRouterHolder;
import com.farcr.nomansland.common.world.watershed.WatershedNoiseRouter;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseRouter.class)
public class NoiseRouterMixin implements WatershedNoiseRouterHolder {
    @Unique
    public WatershedNoiseRouter nml$watershedNoiseRouter;

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
        try {
            RegistryAccess.Frozen registries = ServerLifecycleHooks.getCurrentServer().registryAccess();
            this.nml$watershedNoiseRouter.setDensityFunctions(registries.lookupOrThrow(Registries.NOISE), registries.lookupOrThrow(Registries.DENSITY_FUNCTION));
        } catch (Exception e) {
            NoMansLand.LOGGER.warn(e.getMessage());
        }
    }

    @Inject(method = "mapAll", at = @At("TAIL"))
    private void nml$mapAll(DensityFunction.Visitor visitor, CallbackInfoReturnable<NoiseRouter> cir) {
        this.nml$watershedNoiseRouter.mapAll(visitor);
    }

    @Override
    public WatershedNoiseRouter nml$watershedNoiseRouter() {
        return this.nml$watershedNoiseRouter;
    }
}
