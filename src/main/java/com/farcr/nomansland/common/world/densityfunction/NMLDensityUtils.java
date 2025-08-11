package com.farcr.nomansland.common.world.densityfunction;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class NMLDensityUtils {
    public static DensityFunction rangeSelect(DensityFunction input, double min, double max, double gradient) {
        return new RangeSelectDensityFunction(input, min, max, gradient);
    }

    public static DensityFunction smoothMin(DensityFunction arg1, DensityFunction arg2, double smoothness) {
        return smoothMin(arg1, arg2, DensityFunctions.constant(smoothness));
    }

    public static DensityFunction smoothMin(DensityFunction arg1, DensityFunction arg2, DensityFunction smoothness) {
        return SmoothMixDensityFunction.create(SmoothMixDensityFunction.Type.MIN, arg1, arg2, smoothness);
    }

    public static DensityFunction smoothMax(DensityFunction arg1, DensityFunction arg2, double smoothness) {
        return smoothMax(arg1, arg2, DensityFunctions.constant(smoothness));
    }

    public static DensityFunction smoothMax(DensityFunction arg1, DensityFunction arg2, DensityFunction smoothness) {
        return SmoothMixDensityFunction.create(SmoothMixDensityFunction.Type.MAX, arg1, arg2, smoothness);
    }

    public static DensityFunction mapRange(DensityFunction input, double inMin, double inMax, double outMin, double outMax) {
        return new RemapDensityFunction(input, inMin, inMax, outMin, outMax, false);
    }

    public static DensityFunction mapRangeClamped(DensityFunction input, double inMin, double inMax, double outMin, double outMax) {
        return new RemapDensityFunction(input, inMin, inMax, outMin, outMax, true);
    }
}
