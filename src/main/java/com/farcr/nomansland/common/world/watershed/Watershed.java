package com.farcr.nomansland.common.world.watershed;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

import java.util.function.Supplier;

public record Watershed(
        int watershedX, int watershedZ,
        boolean hasRiver, River river,
        int sourceX, int sourceZ, int sourceHeight,
        int drainX, int drainZ, int drainHeight) {
    // size of a watershed cell
    public static final int WATERSHED_SIZE = 800;

    // probability that a given watershed cell contains a river.
    public static final Supplier<DensityFunction> WATERSHED_PROBABILITY = () -> {
        return DensityFunctions.constant(1.0);
        //return DensityFunctions.noise(Holder.direct(new NormalNoise.NoiseParameters(1, 0)));
    };
    // gradient of watershed source basin
    public static final Supplier<DensityFunction> WATERSHED_SOURCE_HEIGHT = () -> {
        return DensityFunctions.constant(30);
//        return DensityFunctions.add(
//                DensityFunctions.constant(10),
//                DensityFunctions.mul(
//                        DensityFunctions.constant(20),
//                        DensityFunctions.noise(Holder.direct(new NormalNoise.NoiseParameters(1, 0)))
//                )
//        );
    };
    // gradient of watershed drain basin
    public static final Supplier<DensityFunction> WATERSHED_DRAIN_HEIGHT = () -> {
        return DensityFunctions.constant(-35);
//        return DensityFunctions.add(
//                DensityFunctions.constant(-35),
//                DensityFunctions.mul(
//                        DensityFunctions.constant(10),
//                        DensityFunctions.noise(Holder.direct(new NormalNoise.NoiseParameters(1, 0)))
//                )
//        );
    };


}
