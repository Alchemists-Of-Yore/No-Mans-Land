package com.farcr.nomansland.common.world.densityfunction;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

public class NMLDensityUtils {
    public static DensityFunction rangeSelect(DensityFunction input, double min, double max, double gradient) {
        return new RangeSelectDensityFunction(input, min, max, gradient);
    }

    public static DensityFunction smoothMin(double smoothness, DensityFunction arg1, DensityFunction arg2) {
        return smoothMin(DensityFunctions.constant(smoothness), arg1, arg2);
    }

    public static DensityFunction smoothMin(DensityFunction smoothness, DensityFunction arg1, DensityFunction arg2) {
        return SmoothMixDensityFunction.create(SmoothMixDensityFunction.Type.MIN, arg1, arg2, smoothness);
    }

    public static DensityFunction smoothMax(double smoothness, DensityFunction arg1, DensityFunction arg2) {
        return smoothMax(DensityFunctions.constant(smoothness), arg1, arg2);
    }

    public static DensityFunction smoothMax(DensityFunction smoothness, DensityFunction arg1, DensityFunction arg2) {
        return SmoothMixDensityFunction.create(SmoothMixDensityFunction.Type.MAX, arg1, arg2, smoothness);
    }

    public static DensityFunction mapRange(double inMin, double inMax, double outMin, double outMax, DensityFunction input) {
        return new RemapDensityFunction(input, inMin, inMax, outMin, outMax, false);
    }

    public static DensityFunction mapRangeClamped(double inMin, double inMax, double outMin, double outMax, DensityFunction input) {
        return new RemapDensityFunction(input, inMin, inMax, outMin, outMax, true);
    }
}
