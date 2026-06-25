package com.farcr.nomansland.common.world.structure.cave;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public final class CaveContext {
    public static final NormalNoise MEANDER_NOISE = NormalNoise.create(RandomSource.create(48271L), -1, 1.0, 0.5);
    public static final NormalNoise WEATHER_NOISE = NormalNoise.create(RandomSource.create(95791L), -2, 1.0, 1.0, 0.6);

    private static final int LOWEST_CARVE_HEIGHT_ABOVE_WORLD_BOTTOM = 6;

    public final WorldGenLevel level;
    public final BoundingBox chunkBounds;
    public final Occupancy occupancy;
    public final int lowestY;

    public CaveContext(WorldGenLevel level, BoundingBox chunkBounds, PiecesContainer pieces) {
        this.level = level;
        this.chunkBounds = chunkBounds;
        this.occupancy = new Occupancy(pieces);
        this.lowestY = level.getMinBuildHeight() + LOWEST_CARVE_HEIGHT_ABOVE_WORLD_BOTTOM;
    }

    public boolean isCarvableTerrain(BlockState state, BlockPos position) {
        return position.getY() >= this.lowestY
                && !this.occupancy.contains(position)
                && state.is(BlockTags.OVERWORLD_CARVER_REPLACEABLES);
    }

    public BlockState surroundingStone(BlockPos position) {
        return position.getY() <= 0 ? Blocks.DEEPSLATE.defaultBlockState() : Blocks.STONE.defaultBlockState();
    }

    public boolean touchesExteriorAir(BlockPos position, BlockPos.MutableBlockPos neighbour) {
        for (Direction direction : Direction.values()) {
            neighbour.set(position.getX() + direction.getStepX(), position.getY() + direction.getStepY(), position.getZ() + direction.getStepZ());
            if (this.level.getBlockState(neighbour).isAir() && !this.occupancy.contains(neighbour)) return true;
        }
        return false;
    }

    public boolean supportsNeighborAttachment(BlockPos position, BlockPos.MutableBlockPos neighbour) {
        for (Direction direction : Direction.values()) {
            neighbour.set(position.getX() + direction.getStepX(), position.getY() + direction.getStepY(), position.getZ() + direction.getStepZ());
            BlockState neighbourState = this.level.getBlockState(neighbour);
            if (neighbourState.isAir() || !neighbourState.getFluidState().isEmpty()) continue;
            if (neighbourState.isCollisionShapeFullBlock(this.level, neighbour)) continue;
            return true;
        }
        return false;
    }

    public static double signedNoise(double x, double y, double z) {
        return Math.clamp(MEANDER_NOISE.getValue(x, y, z), -1.0, 1.0);
    }

    public static double noise01(double x, double y, double z) {
        return Math.clamp(WEATHER_NOISE.getValue(x, y, z) * 0.5 + 0.5, 0.0, 1.0);
    }
}
