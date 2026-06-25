package com.farcr.nomansland.common.world.structure.cave;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public final class CaveEncasing {
    private static final double WEATHER_FREQ = 0.14;
    private static final int ENCASE_SIDE_RADIUS = 2;
    private static final int ENCASE_ABOVE_RADIUS = 3;
    private static final int ENCASE_BELOW_RADIUS = 2;
    private static final double ENCASE_EDGE_NOISE = 0.35;
    private static final int POCKET_MIN_DEPTH = 2;
    private static final int POCKET_MAX_DEPTH = 5;

    private CaveEncasing() {
    }

    public static int affectedReach() {
        return Math.max(ENCASE_ABOVE_RADIUS + 1, POCKET_MAX_DEPTH + 1);
    }

    public static void apply(CaveContext ctx) {
        List<Hole> wallHoles = detectWallHoles(ctx);
        LongOpenHashSet pocketAir = new LongOpenHashSet();
        carveHolePockets(ctx, wallHoles, pocketAir);
        encaseExposedFaces(ctx, pocketAir);
    }

    private record Hole(BlockPos position, Direction outward) {
    }

    private static List<Hole> detectWallHoles(CaveContext ctx) {
        WorldGenLevel level = ctx.level;
        BoundingBox chunkBounds = ctx.chunkBounds;
        Occupancy occupancy = ctx.occupancy;
        int lowestY = ctx.lowestY;
        List<Hole> holes = new ArrayList<>();
        BlockPos.MutableBlockPos interior = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        for (BoundingBox bounds : occupancy.rooms()) {
            int minX = Math.max(chunkBounds.minX() - POCKET_MAX_DEPTH, bounds.minX());
            int maxX = Math.min(chunkBounds.maxX() + POCKET_MAX_DEPTH, bounds.maxX());
            int minY = Math.max(Math.max(chunkBounds.minY() - POCKET_MAX_DEPTH, bounds.minY()), lowestY);
            int maxY = Math.min(chunkBounds.maxY() + POCKET_MAX_DEPTH, bounds.maxY());
            int minZ = Math.max(chunkBounds.minZ() - POCKET_MAX_DEPTH, bounds.minZ());
            int maxZ = Math.min(chunkBounds.maxZ() + POCKET_MAX_DEPTH, bounds.maxZ());
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        interior.set(x, y, z);
                        if (!level.getBlockState(interior).isAir() || !occupancy.contains(interior)) continue;
                        for (Direction direction : Direction.values()) {
                            neighbour.set(x + direction.getStepX(), y + direction.getStepY(), z + direction.getStepZ());
                            if (level.getBlockState(neighbour).isAir() && !occupancy.contains(neighbour)) {
                                holes.add(new Hole(interior.immutable(), direction));
                                break;
                            }
                        }
                    }
                }
            }
        }
        return holes;
    }

    private static void carveHolePockets(CaveContext ctx, List<Hole> holes, LongOpenHashSet pocketAir) {
        BlockPos.MutableBlockPos center = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos cell = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos above = new BlockPos.MutableBlockPos();
        for (Hole hole : holes) {
            RandomSource holeRandom = RandomSource.create((long) hole.position().getX() * 7919L ^ (long) hole.position().getY() * 104729L ^ (long) hole.position().getZ() * 1299721L);
            int depth = POCKET_MIN_DEPTH + holeRandom.nextInt(POCKET_MAX_DEPTH - POCKET_MIN_DEPTH + 1);
            Direction outward = hole.outward();
            center.set(hole.position());
            for (int step = 1; step <= depth; step++) {
                center.move(outward);
                int radius = step < depth ? 1 : 0;
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        for (int dz = -radius; dz <= radius; dz++) {
                            int manhattan = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                            boolean carveThis = manhattan <= 1 || CaveContext.noise01((center.getX() + dx) * WEATHER_FREQ, (center.getY() + dy) * WEATHER_FREQ, (center.getZ() + dz) * WEATHER_FREQ) <= 0.45;
                            if (!carveThis) continue;
                            cell.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                            carvePocketCell(ctx, cell, above, pocketAir);
                        }
                    }
                }
            }
        }
    }

    private static void carvePocketCell(CaveContext ctx, BlockPos cell, BlockPos.MutableBlockPos above, LongOpenHashSet pocketAir) {
        WorldGenLevel level = ctx.level;
        if (!ctx.chunkBounds.isInside(cell) || cell.getY() < ctx.lowestY) return;
        if (ctx.occupancy.contains(cell)) return;
        if (!level.getBlockState(cell).is(BlockTags.OVERWORLD_CARVER_REPLACEABLES)) return;
        above.set(cell.getX(), cell.getY() + 1, cell.getZ());
        BlockState aboveState = level.getBlockState(above);
        if (!aboveState.isAir() && !pocketAir.contains(above.asLong()) && !ctx.isCarvableTerrain(aboveState, above)) return;
        level.setBlock(cell, Blocks.AIR.defaultBlockState(), 2);
        pocketAir.add(cell.asLong());
    }

    private static void encaseExposedFaces(CaveContext ctx, LongOpenHashSet pocketAir) {
        WorldGenLevel level = ctx.level;
        BoundingBox chunkBounds = ctx.chunkBounds;
        Occupancy occupancy = ctx.occupancy;
        int lowestY = ctx.lowestY;
        int reach = Math.max(ENCASE_SIDE_RADIUS, ENCASE_ABOVE_RADIUS);

        List<BlockPos> exposedFaces = new ArrayList<>();
        BlockPos.MutableBlockPos face = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos neighbour = new BlockPos.MutableBlockPos();
        for (BoundingBox pieceBounds : occupancy.rooms()) {
            int minX = Math.max(chunkBounds.minX() - reach, pieceBounds.minX());
            int maxX = Math.min(chunkBounds.maxX() + reach, pieceBounds.maxX());
            int minY = Math.max(Math.max(chunkBounds.minY() - reach, pieceBounds.minY()), lowestY);
            int maxY = Math.min(chunkBounds.maxY() + reach, pieceBounds.maxY());
            int minZ = Math.max(chunkBounds.minZ() - reach, pieceBounds.minZ());
            int maxZ = Math.min(chunkBounds.maxZ() + reach, pieceBounds.maxZ());
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    for (int y = minY; y <= maxY; y++) {
                        face.set(x, y, z);
                        if (level.getBlockState(face).isAir()) continue;
                        if (ctx.touchesExteriorAir(face, neighbour)) exposedFaces.add(face.immutable());
                    }
                }
            }
        }

        BlockPos.MutableBlockPos filled = new BlockPos.MutableBlockPos();
        for (BlockPos faceBlock : exposedFaces) {
            for (int offsetX = -ENCASE_SIDE_RADIUS; offsetX <= ENCASE_SIDE_RADIUS; offsetX++) {
                for (int offsetZ = -ENCASE_SIDE_RADIUS; offsetZ <= ENCASE_SIDE_RADIUS; offsetZ++) {
                    for (int offsetY = -ENCASE_BELOW_RADIUS; offsetY <= ENCASE_ABOVE_RADIUS; offsetY++) {
                        filled.set(faceBlock.getX() + offsetX, faceBlock.getY() + offsetY, faceBlock.getZ() + offsetZ);
                        if (!chunkBounds.isInside(filled) || filled.getY() < lowestY) continue;
                        if (!level.getBlockState(filled).isAir() || occupancy.contains(filled)) continue;
                        if (leadsToOpenPocket(filled, pocketAir, neighbour)) continue;
                        double normalizedX = (double) offsetX / ENCASE_SIDE_RADIUS;
                        double normalizedZ = (double) offsetZ / ENCASE_SIDE_RADIUS;
                        double normalizedY = offsetY >= 0 ? (double) offsetY / ENCASE_ABOVE_RADIUS : (double) offsetY / ENCASE_BELOW_RADIUS;
                        double distance = normalizedX * normalizedX + normalizedY * normalizedY + normalizedZ * normalizedZ;
                        double threshold = 1.0 - CaveContext.noise01(filled.getX() * WEATHER_FREQ, filled.getY() * WEATHER_FREQ, filled.getZ() * WEATHER_FREQ) * ENCASE_EDGE_NOISE;
                        if (distance <= threshold) {
                            level.setBlock(filled, ctx.surroundingStone(filled), 2);
                        }
                    }
                }
            }
        }
    }

    private static boolean leadsToOpenPocket(BlockPos position, LongOpenHashSet pocketAir, BlockPos.MutableBlockPos neighbour) {
        if (pocketAir.isEmpty()) return false;
        if (pocketAir.contains(position.asLong())) return true;
        for (Direction direction : Direction.values()) {
            neighbour.set(position.getX() + direction.getStepX(), position.getY() + direction.getStepY(), position.getZ() + direction.getStepZ());
            if (pocketAir.contains(neighbour.asLong())) return true;
        }
        return false;
    }
}
