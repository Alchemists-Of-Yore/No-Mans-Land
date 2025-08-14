package com.farcr.nomansland.common.world.watershed;

import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

import java.util.function.Supplier;

public record Watershed(
        int watershedX, int watershedZ,
        boolean hasRiver, River river,
        int sourceX, int sourceZ, int sourceHeight,
        int drainX, int drainZ, int drainHeight) {
    // size of a watershed cell
    public static final int WATERSHED_SIZE = 512;
}
