package com.farcr.nomansland.common.world.watershed;

import com.farcr.nomansland.common.world.densityfunction.CaveRiverDistanceDensityFunction;
import net.minecraft.world.level.levelgen.DensityFunction;

public class WatershedDensityFunctionVisitor implements DensityFunction.Visitor {
    final WatershedMap watershedMap;

    public WatershedDensityFunctionVisitor(WatershedMap watershedMap) {
        this.watershedMap = watershedMap;
    }

    @Override
    public DensityFunction apply(DensityFunction densityFunction) {
        if (densityFunction instanceof CaveRiverDistanceDensityFunction caveRiverDensityFunction) {
            return new CaveRiverDistanceDensityFunction(caveRiverDensityFunction.riverXOffset(), caveRiverDensityFunction.riverZOffset(), this.watershedMap);
        }
        return densityFunction;
    }
}
