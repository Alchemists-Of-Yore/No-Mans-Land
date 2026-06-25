package com.farcr.nomansland.common.world.structure.cave;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

import java.util.Arrays;

public final class CaveTunnel {
    public static final int TUNNEL_LENGTH = 7;
    public static final int TUNNEL_OVERSHOOT = 3;
    public static final int MIN_TUNNEL_HALF = 1;
    public static final int MAX_TUNNEL_HALF = 4;

    private static final double TUNNEL_WANDER = 2.2;
    private static final double TUNNEL_VERTICAL_WANDER = 1.4;
    private static final double TUNNEL_BULGE = 1.3;
    private static final double TUNNEL_ROUGHNESS = 0.34;
    private static final double TUNNEL_MEANDER_FREQ = 0.22;
    private static final double TUNNEL_RADIUS_FREQ = 0.30;
    private static final double TUNNEL_ROUGH_FREQ = 0.45;
    private static final double TUNNEL_SEAL_BAND = 0.55;
    private static final double TUNNEL_MAX_NORMALIZED = 1.0 + TUNNEL_ROUGHNESS + TUNNEL_SEAL_BAND + 0.05;
    private static final int TUNNEL_STEPS_PER_BLOCK = 2;

    private CaveTunnel() {
    }

    public static void carve(CaveContext ctx, BlockPos mouth, Direction directionToCave, int baseHalfWidth, int baseHalfHeight) {
        WorldGenLevel level = ctx.level;
        BoundingBox chunkBounds = ctx.chunkBounds;
        Direction sideways = directionToCave.getClockWise();
        double forwardX = directionToCave.getStepX();
        double forwardZ = directionToCave.getStepZ();
        double sideX = sideways.getStepX();
        double sideZ = sideways.getStepZ();

        double originX = mouth.getX() + 0.5;
        double originY = mouth.getY() + 0.5;
        double originZ = mouth.getZ() + 0.5;

        int length = TUNNEL_LENGTH + TUNNEL_OVERSHOOT;
        int samples = length * TUNNEL_STEPS_PER_BLOCK;
        double phaseA = mouth.getX() * 0.37 + mouth.getZ() * 0.11;
        double phaseB = mouth.getZ() * 0.41 - mouth.getX() * 0.13;

        int spanX = chunkBounds.getXSpan();
        int spanZ = chunkBounds.getZSpan();
        int[] columnTop = new int[spanX * spanZ];
        int[] columnBottom = new int[spanX * spanZ];
        Arrays.fill(columnTop, Integer.MIN_VALUE);
        Arrays.fill(columnBottom, Integer.MAX_VALUE);

        for (int i = 0; i <= samples; i++) {
            double progress = (double) i / samples;
            double forward = progress * length;
            double envelope = Math.sin(Math.PI * progress);

            double lateral = CaveContext.signedNoise(forward * TUNNEL_MEANDER_FREQ, phaseA, phaseB) * TUNNEL_WANDER * envelope;
            double vertical = CaveContext.signedNoise(forward * TUNNEL_MEANDER_FREQ + 53.7, phaseA + 17.3, phaseB - 9.1) * TUNNEL_VERTICAL_WANDER * envelope;
            double radiusNoise = CaveContext.signedNoise(forward * TUNNEL_RADIUS_FREQ + 101.5, phaseB + 4.2, phaseA - 6.6);

            double halfWidth = Math.clamp(baseHalfWidth + 0.6 + envelope * TUNNEL_BULGE + radiusNoise * 0.9, 1.4, MAX_TUNNEL_HALF + 1.6);
            double halfHeight = Math.clamp(baseHalfHeight + 0.4 + envelope * TUNNEL_BULGE * 0.7 + radiusNoise * 0.6, 1.2, MAX_TUNNEL_HALF + 1.0);

            double centerX = originX + forwardX * forward + sideX * lateral;
            double centerY = originY + vertical;
            double centerZ = originZ + forwardZ * forward + sideZ * lateral;

            accumulateTunnelBlob(ctx, centerX, centerY, centerZ, halfWidth, halfHeight, columnTop, columnBottom, spanZ);
        }

        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
        for (int localX = 0; localX < spanX; localX++) {
            for (int localZ = 0; localZ < spanZ; localZ++) {
                int index = localX * spanZ + localZ;
                int top = columnTop[index];
                if (top == Integer.MIN_VALUE) continue;
                carveTunnelColumn(ctx, chunkBounds.minX() + localX, chunkBounds.minZ() + localZ, top, columnBottom[index], position);
            }
        }
    }

    private static void accumulateTunnelBlob(CaveContext ctx, double centerX, double centerY, double centerZ, double halfWidth, double halfHeight, int[] columnTop, int[] columnBottom, int spanZ) {
        BoundingBox chunkBounds = ctx.chunkBounds;
        int minX = Math.max(chunkBounds.minX(), Mth.floor(centerX - halfWidth) - 1);
        int maxX = Math.min(chunkBounds.maxX(), Mth.floor(centerX + halfWidth) + 1);
        int minY = Mth.floor(centerY - halfHeight) - 1;
        int maxY = Mth.floor(centerY + halfHeight) + 1;
        int minZ = Math.max(chunkBounds.minZ(), Mth.floor(centerZ - halfWidth) - 1);
        int maxZ = Math.min(chunkBounds.maxZ(), Mth.floor(centerZ + halfWidth) + 1);
        double invWidth = 1.0 / (halfWidth + 0.5);
        double invHeight = 1.0 / (halfHeight + 0.5);

        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; x++) {
            double dx = (x + 0.5 - centerX) * invWidth;
            double dx2 = dx * dx;
            int columnBase = (x - chunkBounds.minX()) * spanZ - chunkBounds.minZ();
            for (int z = minZ; z <= maxZ; z++) {
                double dz = (z + 0.5 - centerZ) * invWidth;
                double horizontal = dx2 + dz * dz;
                int index = columnBase + z;
                for (int y = minY; y <= maxY; y++) {
                    double dy = (y + 0.5 - centerY) * invHeight;
                    double distance = horizontal + dy * dy;
                    if (distance > TUNNEL_MAX_NORMALIZED) continue;
                    double rough = (CaveContext.noise01(x * TUNNEL_ROUGH_FREQ, y * TUNNEL_ROUGH_FREQ, z * TUNNEL_ROUGH_FREQ) - 0.45) * TUNNEL_ROUGHNESS;
                    double edge = 1.0 + rough;
                    if (distance <= edge) {
                        if (y > columnTop[index]) columnTop[index] = y;
                        if (y < columnBottom[index]) columnBottom[index] = y;
                    } else if (distance <= edge + TUNNEL_SEAL_BAND) {
                        position.set(x, y, z);
                        sealLeakingBlock(ctx, position);
                    }
                }
            }
        }
    }

    private static void carveTunnelColumn(CaveContext ctx, int x, int z, int top, int bottom, BlockPos.MutableBlockPos position) {
        WorldGenLevel level = ctx.level;
        for (int y = top; y >= bottom; y--) {
            position.set(x, y, z);
            BlockState state = level.getBlockState(position);
            if (state.isAir()) continue;
            if (ctx.isCarvableTerrain(state, position)) {
                level.setBlock(position, Blocks.AIR.defaultBlockState(), 2);
            } else {
                return;
            }
        }
    }

    private static void sealLeakingBlock(CaveContext ctx, BlockPos position) {
        if (!ctx.chunkBounds.isInside(position) || position.getY() < ctx.lowestY) return;
        if (ctx.occupancy.contains(position)) return;
        BlockState state = ctx.level.getBlockState(position);
        boolean wouldLeak = state.getBlock() instanceof FallingBlock || !state.getFluidState().isEmpty();
        if (wouldLeak) {
            ctx.level.setBlock(position, ctx.surroundingStone(position), 2);
        }
    }

    public static int tunnelHalfWidth(BoundingBox entranceBounds, Direction directionToCave) {
        int faceWidth = directionToCave.getAxis() == Direction.Axis.Z ? entranceBounds.getXSpan() : entranceBounds.getZSpan();
        return Math.clamp((faceWidth - 2) / 2, MIN_TUNNEL_HALF, MAX_TUNNEL_HALF);
    }

    public static int tunnelHalfHeight(BoundingBox entranceBounds) {
        return Math.clamp((entranceBounds.getYSpan() - 2) / 2, MIN_TUNNEL_HALF, MAX_TUNNEL_HALF);
    }

    public static int affectedReach() {
        return MAX_TUNNEL_HALF + 2 + (int) Math.ceil(TUNNEL_WANDER + TUNNEL_BULGE);
    }

    public static AABB clearanceBox(BoundingBox structureBounds, int groundLevelDelta, Direction directionToCave) {
        int corridorY = structureBounds.minY() + groundLevelDelta + 1;
        int faceX;
        int faceZ;
        if (directionToCave.getAxis() == Direction.Axis.Z) {
            faceX = (structureBounds.minX() + structureBounds.maxX()) / 2;
            faceZ = directionToCave == Direction.NORTH ? structureBounds.minZ() - 1 : structureBounds.maxZ() + 1;
        } else {
            faceZ = (structureBounds.minZ() + structureBounds.maxZ()) / 2;
            faceX = directionToCave == Direction.WEST ? structureBounds.minX() - 1 : structureBounds.maxX() + 1;
        }
        int halfWidth = tunnelHalfWidth(structureBounds, directionToCave);
        int halfHeight = tunnelHalfHeight(structureBounds);
        int corridorCenterY = corridorY - 1 + halfHeight;
        int endX = faceX + directionToCave.getStepX() * (TUNNEL_LENGTH + TUNNEL_OVERSHOOT + 1);
        int endZ = faceZ + directionToCave.getStepZ() * (TUNNEL_LENGTH + TUNNEL_OVERSHOOT + 1);
        int sidePadding = halfWidth + (int) Math.ceil(TUNNEL_WANDER + TUNNEL_BULGE) + 1;
        int verticalPadding = halfHeight + (int) Math.ceil(TUNNEL_VERTICAL_WANDER + TUNNEL_BULGE) + 1;
        int minX;
        int maxX;
        int minZ;
        int maxZ;
        if (directionToCave.getAxis() == Direction.Axis.X) {
            minX = Math.min(faceX, endX);
            maxX = Math.max(faceX, endX);
            minZ = faceZ - sidePadding;
            maxZ = faceZ + sidePadding;
        } else {
            minZ = Math.min(faceZ, endZ);
            maxZ = Math.max(faceZ, endZ);
            minX = faceX - sidePadding;
            maxX = faceX + sidePadding;
        }
        return new AABB(
                minX, corridorCenterY - verticalPadding, minZ,
                maxX + 1, corridorCenterY + verticalPadding + 1, maxZ + 1
        );
    }
}
