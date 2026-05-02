package com.farcr.nomansland.common.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StructureFeatureHelper {
    private StructureFeatureHelper() {}

    public static void replacePlaceholders(
        Map<Block, ResourceKey<ConfiguredFeature<?, ?>>> placeholders,
        WorldGenLevel level,
        ChunkGenerator generator,
        RandomSource random,
        ChunkPos chunkPos,
        PiecesContainer pieces
    ) {
        if (placeholders.isEmpty() || pieces.pieces().isEmpty()) return;

        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMaxX = chunkPos.getMaxBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        int chunkMaxZ = chunkPos.getMaxBlockZ();

        List<BoundingBox> pieceBoxes = pieces.pieces().stream()
            .map(StructurePiece::getBoundingBox)
            .filter(b -> b.maxX() >= chunkMinX && b.minX() <= chunkMaxX
                && b.maxZ() >= chunkMinZ && b.minZ() <= chunkMaxZ)
            .toList();

        if (pieceBoxes.isEmpty()) return;

        var featureRegistry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        Map<BlockPos, ResourceKey<ConfiguredFeature<?, ?>>> features = new HashMap<>();

        for (BoundingBox pb : pieceBoxes) {
            int x0 = Math.max(pb.minX(), chunkMinX);
            int x1 = Math.min(pb.maxX(), chunkMaxX);
            int z0 = Math.max(pb.minZ(), chunkMinZ);
            int z1 = Math.min(pb.maxZ(), chunkMaxZ);

            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    for (int y = pb.minY(); y <= pb.maxY(); y++) {
                        cursor.set(x, y, z);
                        BlockState state = level.getBlockState(cursor);
                        ResourceKey<ConfiguredFeature<?, ?>> featureKey = placeholders.get(state.getBlock());
                        if (featureKey == null) continue;

                        BlockState replacement = Blocks.AIR.defaultBlockState();
                        for (Direction direction : Direction.values()) {
                            if (direction != Direction.DOWN && level.getBlockState(cursor.relative(direction)).is(Blocks.WATER)) {
                                replacement = Blocks.WATER.defaultBlockState();
                                break;
                            }
                        }

                        BlockPos placePos = cursor.immutable();
                        level.setBlock(placePos, replacement, 2);
                        features.put(placePos, featureKey);
                    }
                }
            }
        }

        for (Map.Entry<BlockPos, ResourceKey<ConfiguredFeature<?, ?>>> entry : features.entrySet()) {
            featureRegistry.getHolder(entry.getValue()).ifPresent(holder ->
                holder.value().place(level, generator, random, entry.getKey())
            );
        }
    }
}
