package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.watershed.Watershed;
import com.farcr.nomansland.common.world.watershed.WatershedMap;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomState.class)
public abstract class RandomStateMixin implements RandomStateExtension {
    @Unique
    WatershedMap nml$watershedMap;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nml$init(NoiseGeneratorSettings settings, HolderGetter noiseParametersGetter, long levelSeed, CallbackInfo ci) {
        this.nml$watershedMap = new WatershedMap((RandomState)(Object)this, 256);


    }

    @Override
    public Watershed nml$getWatershed(int blockX, int blockZ) {
        return nml$watershedMap.getOrCreateWatershed(Math.floorDiv(blockX, Watershed.WATERSHED_SIZE), Math.floorDiv(blockZ, Watershed.WATERSHED_SIZE));
    }
}
