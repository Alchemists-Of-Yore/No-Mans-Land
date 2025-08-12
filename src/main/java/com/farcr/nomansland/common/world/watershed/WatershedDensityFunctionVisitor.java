package com.farcr.nomansland.common.world.watershed;

import com.farcr.nomansland.common.world.densityfunction.CaveRiverDensityFunction;
import com.farcr.nomansland.common.world.densityfunction.CaveRiverDistanceDensityFunction;
import com.farcr.nomansland.common.world.densityfunction.CaveRiverTestDensityFunction;
import net.minecraft.world.level.levelgen.DensityFunction;

public class WatershedDensityFunctionVisitor implements DensityFunction.Visitor {
    final WatershedMap watershedMap;

    public WatershedDensityFunctionVisitor(WatershedMap watershedMap) {
        this.watershedMap = watershedMap;
    }

    @Override
    public DensityFunction apply(DensityFunction densityFunction) {
        if (densityFunction instanceof CaveRiverTestDensityFunction func) {
            return new CaveRiverTestDensityFunction(func.riverRadius(), this.watershedMap);
        }
        if (densityFunction instanceof CaveRiverDensityFunction func) {
            return new CaveRiverDensityFunction(func.horizontalDistance(), func.riverHeight(), func.riverRadius(), this.watershedMap);
        }
        if (densityFunction instanceof CaveRiverDistanceDensityFunction func) {
            return new CaveRiverDistanceDensityFunction(func.horizontal(), this.watershedMap);
        }
        return densityFunction;
    }
}
