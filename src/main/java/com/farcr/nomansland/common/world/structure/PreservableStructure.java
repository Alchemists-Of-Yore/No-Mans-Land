package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.world.saved_data.PreservedStructureData;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

public abstract class PreservableStructure extends Structure {

    protected PreservableStructure(final StructureSettings settings) {
        super(settings);
    }

    protected abstract boolean shouldPreserve();

    protected void afterPlaceStructure(final WorldGenLevel level, final StructureManager structureManager, final ChunkGenerator generator, final RandomSource random, final BoundingBox chunkBounds, final ChunkPos chunkPos, final PiecesContainer pieces) {
    }

    @Override
    public void afterPlace(final WorldGenLevel level, final StructureManager structureManager, final ChunkGenerator generator, final RandomSource random, final BoundingBox chunkBounds, final ChunkPos chunkPos, final PiecesContainer pieces) {
        super.afterPlace(level, structureManager, generator, random, chunkBounds, chunkPos, pieces);
        this.afterPlaceStructure(level, structureManager, generator, random, chunkBounds, chunkPos, pieces);
        if (this.shouldPreserve()) {
            this.capturePreservation(level, chunkBounds, pieces);
        }
    }

    private void capturePreservation(final WorldGenLevel level, final BoundingBox chunkBounds, final PiecesContainer pieces) {
        if (pieces.pieces().isEmpty()) return;

        final long owner = preservationOwner(pieces);
        final BlockPos controllerPos = BlockPos.of(owner);
        final PreservedStructureData data = PreservedStructureData.get(level.getLevel());

        final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        final LongArrayList captured = new LongArrayList();

        for (final StructurePiece piece : pieces.pieces()) {
            final BoundingBox box = piece.getBoundingBox();
            final int x0 = Math.max(box.minX(), chunkBounds.minX());
            final int x1 = Math.min(box.maxX(), chunkBounds.maxX());
            final int y0 = Math.max(box.minY(), chunkBounds.minY());
            final int y1 = Math.min(box.maxY(), chunkBounds.maxY());
            final int z0 = Math.max(box.minZ(), chunkBounds.minZ());
            final int z1 = Math.min(box.maxZ(), chunkBounds.maxZ());
            if (x0 > x1 || y0 > y1 || z0 > z1) continue;

            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    for (int y = y0; y <= y1; y++) {
                        cursor.set(x, y, z);
                        final long packed = cursor.asLong();
                        if (packed == owner) continue;
                        if (isPreservable(level, level.getBlockState(cursor), cursor)) captured.add(packed);
                    }
                }
            }
        }

        if (!captured.isEmpty()) data.register(owner, captured.toLongArray());

        if (chunkBounds.isInside(controllerPos)) {
            level.setBlock(controllerPos, NMLBlocks.PRESERVATION.get().defaultBlockState(), 2);
        }
    }

    private static long preservationOwner(final PiecesContainer pieces) {
        final BoundingBox box = pieces.pieces().get(0).getBoundingBox();
        final int x = (box.minX() + box.maxX()) / 2;
        final int y = (box.minY() + box.maxY()) / 2;
        final int z = (box.minZ() + box.maxZ()) / 2;
        return BlockPos.asLong(x, y, z);
    }

    private static boolean isPreservable(final WorldGenLevel level, final BlockState state, final BlockPos pos) {
        if (state.isAir()) return false;
        if (state.is(NMLBlocks.PRESERVATION.get())) return false;
        if (state.is(NMLTags.PRESERVATION_BLACKLIST)) return false;
        if (state.canBeReplaced()) return false;
        if (state.getCollisionShape(level, pos).isEmpty()) return false;
        return true;
    }
}
