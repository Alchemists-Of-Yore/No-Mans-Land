package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.generation.NMLDensityModifications;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseRouter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public interface NoiseRouterExtension {
    DensityFunction nml$watershedProbabilityNoise();
    DensityFunction nml$watershedSourceHeightNoise();
    DensityFunction nml$watershedDrainHeightNoise();
}
