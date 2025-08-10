package com.farcr.nomansland.common.world.densityfunction;

import com.farcr.nomansland.common.world.watershed.Watershed;
import com.farcr.nomansland.common.world.watershed.WatershedMap;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public record CaveRiverDensityFunction(DensityFunction riverXOffset, DensityFunction riverZOffset, WatershedMap watershedMap) implements DensityFunction.SimpleFunction {
//    public static final KeyDispatchDataCodec<CaveRiverDensityFunction> CODEC = KeyDispatchDataCodec.of(
//            MapCodec.unit(new CaveRiverDensityFunction(null))
//    );

    @Override
    public double compute(FunctionContext context) {
        Watershed watershed = this.watershedMap.watershedAtBlock(context.blockX(), context.blockZ());
        double riverDistanceHorizontal = lineSegmentDistance(
                context.blockX(), context.blockZ(),
                watershed.sourceX(), watershed.sourceZ(),
                watershed.drainX(), watershed.drainZ()
        );
        return 0;
    }

    private double lineSegmentDistance(double px, double py, double ax, double ay, double bx, double by) {
        double bax = bx - ax, bay = by - ay;
        double h = ((px - ax) * bax + (py - ay) * bay) / (bax * bax + bay * bay);
        if (h < 0) h = 0;
        else if (h > 1) h = 1;
        double dx = (px - ax) - bax * h;
        double dy = (py - ay) - bay * h;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return 0;
    }

    @Override
    public KeyDispatchDataCodec<? extends DensityFunction> codec() {
        return null;
    }
}
