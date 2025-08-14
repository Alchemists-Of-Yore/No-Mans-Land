package com.farcr.nomansland.common.world.watershed;

import com.farcr.nomansland.common.world.densityfunction.NMLDensityUtils;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class WatershedNoiseRouter {
    // number of rivers within a cell.
    public DensityFunction riverCount;
    // height of river source basins
    public DensityFunction sourceHeight;
    // height of watershed drain basin
    public DensityFunction drainHeight;

    public WatershedNoiseRouter() {}

    private WatershedNoiseRouter(DensityFunction riverCount, DensityFunction sourceHeight, DensityFunction drainHeight) {
        this.riverCount = riverCount;
        this.sourceHeight = sourceHeight;
        this.drainHeight = drainHeight;
    }

    public void createDensityFunctions(NoiseRouter noiseRouter, HolderGetter<NormalNoise.NoiseParameters> noiseParamsRegistry, HolderGetter<DensityFunction> densityFuncsRegistry) {
        this.riverCount = DensityFunctions.constant(1.0);
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
                riverCount != null ? riverCount.mapAll(visitor) : null,
                sourceHeight != null ? sourceHeight.mapAll(visitor) : null,
                drainHeight != null ? drainHeight.mapAll(visitor) : null
        );
    }
}
