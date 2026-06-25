package com.farcr.nomansland.common.world.structure.cave;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class CaveErosion {
    private static final double EROSION_REDUCTION = 0.7;
    private static final int EROSION_CENTRALITY_RADIUS = 6;
    private static final double EROSION_PATCH_FREQ = 0.09;
    private static final double EROSION_PATCH_BIAS = 0.35;
    private static final int EROSION_REACH_MAX = 8;
    private static final double EROSION_REACH_BASE = 3.5;
    private static final double EROSION_TOP_BIAS = 1.0;
    private static final double EROSION_BOTTOM_BIAS = 0.2;
    private static final double EROSION_CEILING_FACTOR = 0.55;
    private static final double EROSION_FACE_WEIGHT = 0.7;
    private static final double EROSION_CENTRE_STRENGTH = 0.45;
    private static final double EROSION_WOBBLE_FREQ = 0.3;
    private static final double EROSION_WOBBLE = 0.3;
    private static final int EROSION_SAMPLE_RADIUS = 2;
    private static final double EROSION_CONVEXITY = 2.0;
    private static final double EROSION_EXPOSURE_BASELINE = 0.3;
    private static final double EROSION_DETAIL_FREQ = 0.6;
    private static final double EROSION_DETAIL = 0.28;

    private static final int[][] CEILING_NEIGHBORS = ceilingNeighbors();

    private CaveErosion() {
    }

    public static void apply(CaveContext ctx) {
        WorldGenLevel level = ctx.level;
        BoundingBox chunkBounds = ctx.chunkBounds;
        Occupancy occupancy = ctx.occupancy;
        int lowestY = ctx.lowestY;

        BoundingBox structureBounds = occupancy.total();
        int coreMinX = Math.max(chunkBounds.minX(), structureBounds.minX());
        int coreMaxX = Math.min(chunkBounds.maxX(), structureBounds.maxX());
        int coreMinZ = Math.max(chunkBounds.minZ(), structureBounds.minZ());
        int coreMaxZ = Math.min(chunkBounds.maxZ(), structureBounds.maxZ());
        if (coreMinX > coreMaxX || coreMinZ > coreMaxZ) return;

        int reach = EROSION_REACH_MAX;
        int scanMinX = coreMinX - reach;
        int scanMaxX = coreMaxX + reach;
        int scanMinZ = coreMinZ - reach;
        int scanMaxZ = coreMaxZ + reach;
        int scanMinY = Math.max(structureBounds.minY() - reach, level.getMinBuildHeight());
        int scanMaxY = Math.min(structureBounds.maxY() + reach, level.getMaxBuildHeight() - 1);
        int sizeX = scanMaxX - scanMinX + 1;
        int sizeY = scanMaxY - scanMinY + 1;
        int sizeZ = scanMaxZ - scanMinZ + 1;

        boolean[] exteriorAir = new boolean[sizeX * sizeY * sizeZ];
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = scanMinX; x <= scanMaxX; x++) {
            for (int y = scanMinY; y <= scanMaxY; y++) {
                for (int z = scanMinZ; z <= scanMaxZ; z++) {
                    cursor.set(x, y, z);
                    if (level.getBlockState(cursor).isAir() && !occupancy.contains(cursor)) {
                        exteriorAir[((x - scanMinX) * sizeY + (y - scanMinY)) * sizeZ + (z - scanMinZ)] = true;
                    }
                }
            }
        }

        int exposedMinY = Math.max(scanMinY, structureBounds.minY());
        int exposedMaxY = Math.min(scanMaxY, structureBounds.maxY());
        boolean[] exposedColumn = new boolean[sizeX * sizeZ];
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        for (int x = scanMinX; x <= scanMaxX; x++) {
            for (int z = scanMinZ; z <= scanMaxZ; z++) {
                boolean exposed = false;
                for (int y = exposedMaxY; y >= exposedMinY && !exposed; y--) {
                    cursor.set(x, y, z);
                    if (!occupancy.contains(cursor)) continue;
                    if (!level.getBlockState(cursor).is(BlockTags.OVERWORLD_CARVER_REPLACEABLES)) continue;
                    if (ctx.touchesExteriorAir(cursor, neighbour)) exposed = true;
                }
                exposedColumn[(x - scanMinX) * sizeZ + (z - scanMinZ)] = exposed;
            }
        }

        int structureSpanY = Math.max(1, structureBounds.maxY() - structureBounds.minY());
        int erodeTopY = structureBounds.maxY();
        int erodeBottomY = Math.max(lowestY, structureBounds.minY());
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();
        for (int x = coreMinX; x <= coreMaxX; x++) {
            for (int z = coreMinZ; z <= coreMaxZ; z++) {
                double centrality = distanceToUnexposed(exposedColumn, scanMinX, scanMinZ, sizeX, sizeZ, x, z);
                double centerFactor = Math.clamp(centrality / EROSION_CENTRALITY_RADIUS, 0.0, 1.0);
                centerFactor = centerFactor * centerFactor * (3.0 - 2.0 * centerFactor);
                if (centerFactor <= 0.0) continue;
                double patch = 1.0 + (CaveContext.noise01(x * EROSION_PATCH_FREQ, 7.0, z * EROSION_PATCH_FREQ) - 0.5) * 2.0 * EROSION_PATCH_BIAS;

                for (int y = erodeTopY; y >= erodeBottomY; y--) {
                    position.set(x, y, z);
                    if (!occupancy.contains(position)) continue;
                    if (!level.getBlockState(position).is(BlockTags.OVERWORLD_CARVER_REPLACEABLES)) continue;
                    below.set(x, y - 1, z);
                    if (!occupancy.contains(below)) continue;
                    if (ctx.supportsNeighborAttachment(position, neighbour)) continue;

                    double heightFraction = Math.clamp((double) (y - structureBounds.minY()) / structureSpanY, 0.0, 1.0);
                    double heightBias = EROSION_BOTTOM_BIAS + (EROSION_TOP_BIAS - EROSION_BOTTOM_BIAS) * heightFraction;
                    double wobble = 1.0
                            + (CaveContext.noise01(x * EROSION_WOBBLE_FREQ, y * EROSION_WOBBLE_FREQ, z * EROSION_WOBBLE_FREQ) - 0.5) * 2.0 * EROSION_WOBBLE
                            + (CaveContext.noise01(x * EROSION_DETAIL_FREQ, y * EROSION_DETAIL_FREQ, z * EROSION_DETAIL_FREQ) - 0.5) * 2.0 * EROSION_DETAIL;
                    double exposure = exteriorAirFraction(exteriorAir, scanMinX, scanMinY, scanMinZ, sizeX, sizeY, sizeZ, x, y, z, EROSION_SAMPLE_RADIUS);
                    double convexBoost = EROSION_CONVEXITY * Math.max(0.0, exposure - EROSION_EXPOSURE_BASELINE);
                    double weight = EROSION_FACE_WEIGHT + EROSION_CENTRE_STRENGTH * centerFactor + convexBoost;
                    int aboveIndexY = (y + 1) - scanMinY;
                    boolean exposedFromAbove = aboveIndexY < sizeY
                            && exteriorAir[((x - scanMinX) * sizeY + aboveIndexY) * sizeZ + (z - scanMinZ)];
                    double ceilingFactor = exposedFromAbove ? EROSION_CEILING_FACTOR : 1.0;
                    double reachHere = Math.min(EROSION_REACH_MAX, EROSION_REACH_BASE * heightBias * patch * wobble * EROSION_REDUCTION * weight * ceilingFactor);
                    if (reachHere < 1.0) continue;

                    if (exteriorAirWithin(exteriorAir, scanMinX, scanMinY, scanMinZ, sizeX, sizeY, sizeZ, x, y, z, reachHere)) {
                        level.setBlock(position, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
        pruneFloatingCeiling(ctx, exteriorAir, scanMinX, scanMinY, scanMinZ, sizeX, sizeY, sizeZ, coreMinX, coreMaxX, coreMinZ, coreMaxZ, erodeBottomY, erodeTopY);
    }

    private static void pruneFloatingCeiling(CaveContext ctx, boolean[] exteriorAir,
                                             int scanMinX, int scanMinY, int scanMinZ, int sizeX, int sizeY, int sizeZ,
                                             int coreMinX, int coreMaxX, int coreMinZ, int coreMaxZ, int erodeBottomY, int erodeTopY) {
        WorldGenLevel level = ctx.level;
        Occupancy occupancy = ctx.occupancy;
        int scanMaxX = scanMinX + sizeX - 1;
        int scanMaxZ = scanMinZ + sizeZ - 1;
        LongOpenHashSet ceilingRemnants = new LongOpenHashSet();
        LongOpenHashSet buriedCeiling = new LongOpenHashSet();
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos vertical = new BlockPos.MutableBlockPos();
        for (int x = scanMinX; x <= scanMaxX; x++) {
            for (int z = scanMinZ; z <= scanMaxZ; z++) {
                for (int y = erodeBottomY; y <= erodeTopY; y++) {
                    int aboveIndexY = (y + 1) - scanMinY;
                    if (aboveIndexY < 0 || aboveIndexY >= sizeY) continue;
                    position.set(x, y, z);
                    if (!occupancy.contains(position)) continue;
                    if (!level.getBlockState(position).is(BlockTags.OVERWORLD_CARVER_REPLACEABLES)) continue;
                    vertical.set(x, y - 1, z);
                    if (!level.getBlockState(vertical).isAir()) continue;
                    boolean aboveExteriorAir = exteriorAir[((x - scanMinX) * sizeY + aboveIndexY) * sizeZ + (z - scanMinZ)];
                    if (aboveExteriorAir) {
                        ceilingRemnants.add(BlockPos.asLong(x, y, z));
                    } else {
                        vertical.set(x, y + 1, z);
                        if (!occupancy.contains(vertical)) buriedCeiling.add(BlockPos.asLong(x, y, z));
                    }
                }
            }
        }
        if (ceilingRemnants.isEmpty()) return;

        ArrayDeque<Long> queue = new ArrayDeque<>();
        LongOpenHashSet connected = new LongOpenHashSet();
        for (long cell : ceilingRemnants) {
            if (touchesCeilingAnchor(cell, buriedCeiling) && connected.add(cell)) queue.add(cell);
        }
        while (!queue.isEmpty()) {
            long cell = queue.poll();
            int cx = BlockPos.getX(cell);
            int cy = BlockPos.getY(cell);
            int cz = BlockPos.getZ(cell);
            for (int[] offset : CEILING_NEIGHBORS) {
                long neighbour = BlockPos.asLong(cx + offset[0], cy + offset[1], cz + offset[2]);
                if (ceilingRemnants.contains(neighbour) && connected.add(neighbour)) queue.add(neighbour);
            }
        }

        BlockPos.MutableBlockPos neighbourPos = new BlockPos.MutableBlockPos();
        for (long cell : ceilingRemnants) {
            if (connected.contains(cell)) continue;
            int cx = BlockPos.getX(cell);
            int cy = BlockPos.getY(cell);
            int cz = BlockPos.getZ(cell);
            if (cx < coreMinX || cx > coreMaxX || cz < coreMinZ || cz > coreMaxZ) continue;
            position.set(cx, cy, cz);
            if (ctx.supportsNeighborAttachment(position, neighbourPos)) continue;
            level.setBlock(position, Blocks.AIR.defaultBlockState(), 2);
        }
    }

    private static boolean touchesCeilingAnchor(long cell, LongOpenHashSet buriedCeiling) {
        int cx = BlockPos.getX(cell);
        int cy = BlockPos.getY(cell);
        int cz = BlockPos.getZ(cell);
        for (int[] offset : CEILING_NEIGHBORS) {
            if (buriedCeiling.contains(BlockPos.asLong(cx + offset[0], cy + offset[1], cz + offset[2]))) return true;
        }
        return false;
    }

    private static boolean exteriorAirWithin(boolean[] exteriorAir, int scanMinX, int scanMinY, int scanMinZ, int sizeX, int sizeY, int sizeZ, int x, int y, int z, double reach) {
        int radius = Mth.ceil(reach);
        double reachSquared = reach * reach;
        for (int dx = -radius; dx <= radius; dx++) {
            int localX = x + dx - scanMinX;
            if (localX < 0 || localX >= sizeX) continue;
            int dx2 = dx * dx;
            for (int dy = -radius; dy <= radius; dy++) {
                int localY = y + dy - scanMinY;
                if (localY < 0 || localY >= sizeY) continue;
                int planeBase = (localX * sizeY + localY) * sizeZ;
                int dxy2 = dx2 + dy * dy;
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dxy2 + dz * dz > reachSquared) continue;
                    int localZ = z + dz - scanMinZ;
                    if (localZ < 0 || localZ >= sizeZ) continue;
                    if (exteriorAir[planeBase + localZ]) return true;
                }
            }
        }
        return false;
    }

    private static double distanceToUnexposed(boolean[] exposed, int scanMinX, int scanMinZ, int sizeX, int sizeZ, int x, int z) {
        int nearest = EROSION_CENTRALITY_RADIUS * EROSION_CENTRALITY_RADIUS + 1;
        for (int dx = -EROSION_CENTRALITY_RADIUS; dx <= EROSION_CENTRALITY_RADIUS; dx++) {
            for (int dz = -EROSION_CENTRALITY_RADIUS; dz <= EROSION_CENTRALITY_RADIUS; dz++) {
                int distanceSquared = dx * dx + dz * dz;
                if (distanceSquared >= nearest) continue;
                int localX = x + dx - scanMinX;
                int localZ = z + dz - scanMinZ;
                boolean wall = localX < 0 || localX >= sizeX || localZ < 0 || localZ >= sizeZ
                        || !exposed[localX * sizeZ + localZ];
                if (wall) nearest = distanceSquared;
            }
        }
        return Math.sqrt(nearest);
    }

    private static double exteriorAirFraction(boolean[] exteriorAir, int scanMinX, int scanMinY, int scanMinZ, int sizeX, int sizeY, int sizeZ, int x, int y, int z, int radius) {
        int air = 0;
        int total = 0;
        double radiusSquared = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            int localX = x + dx - scanMinX;
            int dx2 = dx * dx;
            for (int dy = -radius; dy <= radius; dy++) {
                int localY = y + dy - scanMinY;
                int dxy2 = dx2 + dy * dy;
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dxy2 + dz * dz > radiusSquared) continue;
                    total++;
                    int localZ = z + dz - scanMinZ;
                    if (localX < 0 || localX >= sizeX || localY < 0 || localY >= sizeY || localZ < 0 || localZ >= sizeZ) continue;
                    if (exteriorAir[(localX * sizeY + localY) * sizeZ + localZ]) air++;
                }
            }
        }
        return total == 0 ? 0.0 : (double) air / total;
    }

    private static int[][] ceilingNeighbors() {
        List<int[]> list = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    list.add(new int[]{dx, dy, dz});
                }
            }
        }
        return list.toArray(new int[0][]);
    }
}
