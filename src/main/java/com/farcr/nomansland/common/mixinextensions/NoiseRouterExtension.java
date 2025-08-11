package com.farcr.nomansland.common.mixinextensions;

import net.minecraft.world.level.levelgen.DensityFunction;

public interface NoiseRouterExtension {
    DensityFunction nml$watershedProbabilityNoise();
    DensityFunction nml$watershedSourceHeightNoise();
    DensityFunction nml$watershedDrainHeightNoise();
}
