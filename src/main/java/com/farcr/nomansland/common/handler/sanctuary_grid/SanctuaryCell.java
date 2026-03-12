package com.farcr.nomansland.common.handler.sanctuary_grid;

import com.farcr.nomansland.common.world.structure.SanctuaryRuinsStructurePlacement;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import org.jetbrains.annotations.Nullable;

//wowee wow weeee
public class SanctuaryCell {

    public static final int MIN_CHUNK_DISTANCE = 5;
    public static final int MAX_CHUNK_DISTANCE = 6_000;

    public final int x;
    public final int z;

    private boolean isValid = true;
    private boolean attemptedToGenerate = false;

    @Nullable
    private ChunkPos firstSanctuaryPos;

    @Nullable
    private ChunkPos secondSanctuaryPos;

    public SanctuaryCell(final int x, final int z) {
        this.x = x;
        this.z = z;
    }

    /**
     * Attempts to generate the pair of sanctuary positions for this cell, with the given adjacent cells for distance checks.
     */
    public void generatePositions(final long levelSeed, final ChunkGeneratorStructureState state, final SanctuaryRuinsStructurePlacement placement, final SanctuaryCell[][] adjacentCells) {
        final long newSeed = (long) this.x * 341873128712L + (long) this.z * 132897987541L + levelSeed;

        final RandomSource source = RandomSource.create(newSeed);
        final RandomSource biomeSource = source.fork();

        for (int i = 0; i < 100; i++) {
            final Pair<BlockPos, Holder<Biome>> firstPosition = this.getSanctuaryPos(state, source, biomeSource);
            if (firstPosition == null || !this.isBlockPosInside(firstPosition.getFirst())) {
                continue;
            }

            final Pair<BlockPos, Holder<Biome>> secondPosition = this.getSanctuaryPos(state, source, biomeSource);
            if (secondPosition == null || !this.isBlockPosInside(secondPosition.getFirst())) {
                continue;
            }

            final ChunkPos firstChunkPos = new ChunkPos(firstPosition.getFirst());
            final ChunkPos secondChunkPos = new ChunkPos(secondPosition.getFirst());

            final int dist = firstChunkPos.distanceSquared(secondChunkPos);
            if (dist < MIN_CHUNK_DISTANCE * MIN_CHUNK_DISTANCE || dist > MAX_CHUNK_DISTANCE * MAX_CHUNK_DISTANCE) {
                continue;
            }

            this.firstSanctuaryPos = firstChunkPos;
            this.secondSanctuaryPos = secondChunkPos;
            break;
        }

        if (this.firstSanctuaryPos == null || this.secondSanctuaryPos == null) {
            this.isValid = false;
        }

        this.attemptedToGenerate = true;
    }

    private @Nullable Pair<BlockPos, Holder<Biome>> getSanctuaryPos(final ChunkGeneratorStructureState state, final RandomSource source, final RandomSource biomeSource) {
        return this.locateValidPosition(state, SanctuaryGrid.CELL_SIDE_CHUNK_LENGTH * source.nextDouble(), SanctuaryGrid.CELL_SIDE_CHUNK_LENGTH * source.nextDouble(), biomeSource);
    }

    private Pair<BlockPos, Holder<Biome>> locateValidPosition(final ChunkGeneratorStructureState state, final double localX, final double localZ, final RandomSource biomeSource) {
        return state.biomeSource.findBiomeHorizontal(
                (int) ((this.x * SanctuaryGrid.CELL_SIDE_CHUNK_LENGTH) + localX) * 16,
                64,
                (int) ((this.z * SanctuaryGrid.CELL_SIDE_CHUNK_LENGTH) + localZ) * 16,
                32,
                biome -> !biome.is(BiomeTags.IS_OCEAN) && !biome.is(BiomeTags.IS_RIVER),
                biomeSource,
                state.randomState().sampler());
    }

    public boolean hasAttemptedToGenerate() {
        return this.attemptedToGenerate;
    }

    public boolean valid() {
        return this.isValid;
    }

    public boolean validGenChunk(final int checkChunkX, final int checkChunkZ) {
        final ChunkPos checkPos = new ChunkPos(checkChunkX, checkChunkZ);
        return checkPos.equals(this.firstSanctuaryPos) || checkPos.equals(this.secondSanctuaryPos);
    }

    public boolean isBlockPosInside(final BlockPos pos) {
        return Math.floorDiv(pos.getX(), SanctuaryGrid.CELL_SIDE_BLOCK_LENGTH) == this.x
                || Math.floorDiv(pos.getZ(), SanctuaryGrid.CELL_SIDE_BLOCK_LENGTH) == this.z;
    }

    public @Nullable ChunkPos getFirstSanctuaryPos() {
        return this.firstSanctuaryPos;
    }

    public @Nullable ChunkPos getSecondSanctuaryPos() {
        return this.secondSanctuaryPos;
    }
}
