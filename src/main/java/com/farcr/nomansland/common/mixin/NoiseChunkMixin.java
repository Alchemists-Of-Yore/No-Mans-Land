package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.mixinextensions.WatershepMapHolder;
import com.farcr.nomansland.common.world.watershed.WatershedMap;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseChunk.class)
public class NoiseChunkMixin implements WatershepMapHolder {
    @Unique
    WatershedMap nml$watershedMap;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nml$init(int cellCountXZ, RandomState random, int firstNoiseX, int firstNoiseZ, NoiseSettings noiseSettings, DensityFunctions.BeardifierOrMarker beardifier, NoiseGeneratorSettings noiseGeneratorSettings, Aquifer.FluidPicker fluidPicker, Blender blendifier, CallbackInfo ci) {
        this.nml$watershedMap = ((WatershepMapHolder)(Object) random).nml$getWatershedMap();
    }

    @Override
    public WatershedMap nml$getWatershedMap() {
        return nml$watershedMap;
    }
}
