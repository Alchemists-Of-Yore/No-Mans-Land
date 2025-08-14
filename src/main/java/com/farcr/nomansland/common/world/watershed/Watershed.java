package com.farcr.nomansland.common.world.watershed;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Aquifer;

import javax.annotation.Nullable;
import java.util.List;

public record Watershed(int watershedX, int watershedZ, List<River> rivers, int drainX, int drainZ, int drainHeight) {
    // size of a watershed cell
    public static final int WATERSHED_SIZE = 512;

    public River.RiverSpaceCoordinates nearestRiverCoordinates(int x, int y, int z) {
        River.MutableRiverSpaceCoords best = new River.MutableRiverSpaceCoords(1000, 0);
        for (River river : rivers) {
            river.sampleDistanceFromRiverRecursive(river.bvhRoot, x, y, z, best);
        }
        return best.toImmutable();
    }

    @Nullable
    public Aquifer.FluidStatus getFluidOverride(int x, int y, int z) {
        if (rivers.isEmpty()) return null;

        River.RiverSpaceCoordinates coordinates = this.nearestRiverCoordinates(x, y, z);
        double distanceToWaterSurface = y - coordinates.riverHeight();
        if (coordinates.horizontalDistance() < 30 && distanceToWaterSurface < 25 && distanceToWaterSurface > -30) {
            return new Aquifer.FluidStatus(River.getWaterSurfaceHeight(coordinates.riverHeight(), 0), Blocks.WATER.defaultBlockState());
        }

        return null;
    }
}
