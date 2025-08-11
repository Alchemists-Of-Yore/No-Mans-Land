package com.farcr.nomansland.common.world.watershed;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.function.Supplier;

public record Watershed(
        int watershedX, int watershedZ,
        boolean hasRiver,
        int sourceX, int sourceZ, int sourceHeight,
        int drainX, int drainZ, int drainHeight) {
    // size of a watershed cell
    public static final int WATERSHED_SIZE = 512;

    // probability that a given watershed cell contains a river.
    public static final Supplier<DensityFunction> WATERSHED_PROBABILITY = () -> {
        return DensityFunctions.noise(Holder.direct(new NormalNoise.NoiseParameters(1, 0)));
    };
    // height of watershed source basin
    public static final Supplier<DensityFunction> WATERSHED_SOURCE_HEIGHT = () -> {
        return DensityFunctions.add(
                DensityFunctions.constant(10),
                DensityFunctions.mul(
                        DensityFunctions.constant(20),
                        DensityFunctions.noise(Holder.direct(new NormalNoise.NoiseParameters(1, 0)))
                )
        );
    };
    // height of watershed drain basin
    public static final Supplier<DensityFunction> WATERSHED_DRAIN_HEIGHT = () -> {
        return DensityFunctions.add(
                DensityFunctions.constant(-35),
                DensityFunctions.mul(
                        DensityFunctions.constant(10),
                        DensityFunctions.noise(Holder.direct(new NormalNoise.NoiseParameters(1, 0)))
                )
        );
    };
}
