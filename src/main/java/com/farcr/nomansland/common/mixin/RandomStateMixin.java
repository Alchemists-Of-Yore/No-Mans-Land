package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.watershed.WatershedDensityFunctionVisitor;
import com.farcr.nomansland.common.world.watershed.WatershedMap;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomState.class)
public abstract class RandomStateMixin {
    @Mutable @Shadow @Final private NoiseRouter router;
    @Unique WatershedMap nml$watershedMap;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nml$init(NoiseGeneratorSettings settings, HolderGetter noiseParametersGetter, long levelSeed, CallbackInfo ci) {
        this.nml$watershedMap = new WatershedMap((RandomState) (Object) this, 256);
        this.router = this.router.mapAll(new WatershedDensityFunctionVisitor(this.nml$watershedMap));
        NoMansLand.LOGGER.info(this.router);
    }
}
