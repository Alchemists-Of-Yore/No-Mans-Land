package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.mixinextensions.WatershedNoiseRouterHolder;
import com.farcr.nomansland.common.mixinextensions.WatershepMapHolder;
import com.farcr.nomansland.common.world.watershed.WatershedDensityFunctionVisitor;
import com.farcr.nomansland.common.world.watershed.WatershedMap;
import com.farcr.nomansland.common.world.watershed.WatershedNoiseRouter;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RandomState.class)
public class RandomStateMixin implements WatershepMapHolder {
    @Mutable @Shadow @Final private NoiseRouter router;
    @Unique WatershedMap nml$watershedMap;

    @Inject(method = "<init>", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void nml$init(NoiseGeneratorSettings settings, HolderGetter noiseParametersGetter, long levelSeed, CallbackInfo ci, boolean flag, DensityFunction.Visitor densityfunction$visitor) {
        this.nml$watershedMap = new WatershedMap((RandomState) (Object) this, 256);
        this.router = this.router.mapAll(new WatershedDensityFunctionVisitor(this.nml$watershedMap));
    }

    @Override
    public WatershedMap nml$getWatershedMap() {
        return nml$watershedMap;
    }
}
