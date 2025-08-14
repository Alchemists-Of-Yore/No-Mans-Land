package com.farcr.nomansland.common.world.watershed;

import com.farcr.nomansland.common.world.densityfunction.NMLDensityUtils;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class WatershedNoiseRouter {
    // probability that a given watershed cell contains a river.
    public DensityFunction probability;
    // height of watershed source basin
    public DensityFunction sourceHeight;
    // height of watershed drain basin
    public DensityFunction drainHeight;

    public WatershedNoiseRouter() {}

    private WatershedNoiseRouter(DensityFunction probability, DensityFunction sourceHeight, DensityFunction drainHeight) {
        this.probability = probability;
        this.sourceHeight = sourceHeight;
        this.drainHeight = drainHeight;
    }

    public void createDensityFunctions(HolderGetter<NormalNoise.NoiseParameters> noiseParamsRegistry, HolderGetter<DensityFunction> densityFuncsRegistry) {
        this.probability = DensityFunctions.constant(1.0);
        this.sourceHeight = NMLDensityUtils.mapRange(
                -1, 1,
                30, -20,
                DensityFunctions.noise(noiseParamsRegistry.getOrThrow(Noises.AQUIFER_BARRIER))
        );
        this.drainHeight = NMLDensityUtils.mapRange(
                -1, 1,
                -20, -40,
                DensityFunctions.noise(noiseParamsRegistry.getOrThrow(Noises.AQUIFER_BARRIER))
        );
    }

    public WatershedNoiseRouter mapAll(DensityFunction.Visitor visitor) {
        return new WatershedNoiseRouter(
                probability != null ? probability.mapAll(visitor) : null,
                sourceHeight != null ? sourceHeight.mapAll(visitor) : null,
                drainHeight != null ? drainHeight.mapAll(visitor) : null
        );
    }
}
