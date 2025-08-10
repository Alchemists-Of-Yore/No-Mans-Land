package com.farcr.nomansland.common.world.watershed;

import com.farcr.nomansland.common.world.densityfunction.CaveRiverDensityFunction;
import net.minecraft.world.level.levelgen.DensityFunction;

public class WatershedDensityFunctionVisitor implements DensityFunction.Visitor {
    final WatershedMap watershedMap;

    public WatershedDensityFunctionVisitor(WatershedMap watershedMap) {
        this.watershedMap = watershedMap;
    }

    @Override
    public DensityFunction apply(DensityFunction densityFunction) {
        if (densityFunction instanceof CaveRiverDensityFunction caveRiverDensityFunction) {
            return new CaveRiverDensityFunction(caveRiverDensityFunction.riverXOffset(), caveRiverDensityFunction.riverZOffset(), this.watershedMap);
        }
        return densityFunction;
    }
}
