package com.farcr.nomansland.utility;

import net.minecraft.util.Mth;

public class MathUtilities {
    public static final double SMOOTH_MIN_NORMALIZATION_FACTOR = 1.0 / (1.0 - Math.sqrt(0.5));
    public static double smoothMin(double a, double b, double k) {
        k *= SMOOTH_MIN_NORMALIZATION_FACTOR;
        double h = Math.max(k - Math.abs(a - b), 0.0) / k;
        return Math.min(a, b) - k * 0.5 * (1.0 + h - Math.sqrt(1.0 - h * (h - 2.0)));
    }
    public static double smoothMinMixFactor(double a, double b, double k) {
        k *= SMOOTH_MIN_NORMALIZATION_FACTOR;
        double h = Math.max(k - Math.abs(a - b), 0.0) / k;
        double x = 0.5 * (1.0 + h - Math.sqrt(1.0 - h * (h - 2.0))) * SMOOTH_MIN_NORMALIZATION_FACTOR * Math.signum(b - a) + Math.signum(a - b);
        return x * 0.5 + 0.5;
    }

    public static double terrace(double x, double frequency, double gradient) {
        double xFloor = Math.floor(x / frequency) * frequency;
        double xMod = (x - xFloor) / frequency;

        if (xMod > 1 - gradient) {
            xMod = Mth.smoothstep(Mth.map(xMod, 1 - gradient, 1, 0, 1));
        } else {
            xMod = 0;
        }

        return xMod * frequency + xFloor;
    }
}
