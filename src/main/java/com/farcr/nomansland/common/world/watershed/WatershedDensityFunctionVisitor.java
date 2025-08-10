package com.farcr.nomansland.common.world.watershed;

import net.minecraft.world.level.levelgen.DensityFunction;

public class WatershedDensityFunctionVisitor implements DensityFunction.Visitor {
    @Override
    public DensityFunction apply(DensityFunction densityFunction) {
        return densityFunction;
    }
}
