package com.farcr.nomansland.common.world.watershed;

import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class WatershedNoiseRouter {
    // probability that a given watershed cell contains a river.
    public DensityFunction probability;
    // height of watershed source basin
    public DensityFunction sourceHeight;
    // height of watershed drain basin
    public DensityFunction drainHeight;

    public WatershedNoiseRouter() {}

    public void setDensityFunctions(HolderGetter<NormalNoise.NoiseParameters> noiseParamsRegistry, HolderGetter<DensityFunction> densityFuncsRegistry) {
        this.probability = DensityFunctions.constant(1.0);
        this.sourceHeight = DensityFunctions.constant(30);
        this.drainHeight = DensityFunctions.constant(-35);
    }

    public void mapAll(DensityFunction.Visitor visitor) {
        if (this.probability != null) this.probability.mapAll(visitor);
        if (this.sourceHeight != null) this.sourceHeight.mapAll(visitor);
        if (this.drainHeight != null) this.drainHeight.mapAll(visitor);
    }
}
