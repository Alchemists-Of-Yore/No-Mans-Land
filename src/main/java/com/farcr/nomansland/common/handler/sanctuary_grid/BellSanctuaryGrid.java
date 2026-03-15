package com.farcr.nomansland.common.handler.sanctuary_grid;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

/**
 * A grid of {@link BellSanctuaryCell cells}. Each cell contains A pair of Bell Sanctuary {@link ChunkPos section positions.}
 */
public class BellSanctuaryGrid extends SavedData {

    /**
     * How many chunks long and tall a single cell is
     */
    public static final int CELL_SIDE_CHUNK_LENGTH = 40;

    /**
     * the block equivalent of {@link BellSanctuaryGrid#CELL_SIDE_CHUNK_LENGTH}
     */
    public static final int CELL_SIDE_BLOCK_LENGTH = CELL_SIDE_CHUNK_LENGTH * 16;

    /**
     * The minimum distance allowed between two Bell Sanctuary position in a pair.
     */
    public static final int MIN_CHUNK_DISTANCE = 60;

    /**
     * The maximum distance allowed between two Bell Sanctuary positions in a pair.
     */
    public static final int MAX_CHUNK_DISTANCE = 350;

    /**
     * Table containing every {@link BellSanctuaryCell cell} for the associated level.
     */
    private final Table<Integer, Integer, BellSanctuaryCell> bellSanctuaryCells;

    private final long levelSeed;

    public BellSanctuaryGrid(final long levelSeed) {
        this.bellSanctuaryCells = HashBasedTable.create();
        this.levelSeed = levelSeed;
    }

    /**
     * Attempts to get a {@link BellSanctuaryCell cell} from the given X and Z block positions.
     *
     * @return The associated {@link BellSanctuaryCell cell}, or null if none has been generated yet.
     */
    @Nullable
    public BellSanctuaryCell getCell(final int blockX, final int blockZ) {
        return this.bellSanctuaryCells.get(Math.floorDiv(blockX, CELL_SIDE_BLOCK_LENGTH), Math.floorDiv(blockZ, CELL_SIDE_BLOCK_LENGTH));
    }

    /**
     * Attempts to generate a new {@link com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryCell.SanctuaryPair Pair} from the given {@link ChunkPos}
     */
    @ApiStatus.Internal
    public boolean tryGeneratePair(final ChunkPos pos) {
        final BellSanctuaryCell firstCell = this.generateOrGetCellChunkPos(pos.x, pos.z, true);

        //early return as we absolutely know this pair already exists
        if (firstCell.containsPosition(pos)) {
            return false;
        }

        final long newSeed = (long) pos.x * 341873128712L + (long) pos.z * 132897987541L + this.levelSeed;
        final RandomSource source = RandomSource.create(newSeed);

        final Vector2d mutVec = new Vector2d(); //I would make this a static field, but this method can in theory be called from multiple threads :btw:

        BellSanctuaryCell secondCell = null;
        BellSanctuaryCell.SanctuaryPair newPair = null;
        for (int i = 0; i < 100; i++) {
            final double randomRad = Math.TAU * (i / 10d + 1) * source.nextDouble();
            final double randomDist = MIN_CHUNK_DISTANCE + (MAX_CHUNK_DISTANCE - MIN_CHUNK_DISTANCE) * source.nextDouble();
            mutVec.set((randomDist * Math.cos(randomRad)) + pos.x, (randomDist * Math.sin(randomRad)) + pos.z);
            final ChunkPos secondPos = new ChunkPos((int) mutVec.x, (int) mutVec.y);

            final BellSanctuaryCell containing = this.generateOrGetCellChunkPos(secondPos.x, secondPos.z, true);
            if (containing.containsPosition(secondPos)) {
                continue;
            }

            //we have a valid new pair
            secondCell = containing;
            newPair = new BellSanctuaryCell.SanctuaryPair(pos, secondPos);
            break;
        }

        if (newPair == null) {
            return false;
        }

        firstCell.addPair(newPair);
        if (firstCell != secondCell) {
            secondCell.addPair(newPair);
        }

        return true;
    }

    private @NotNull BellSanctuaryCell generateOrGetCellChunkPos(final int chunkX, final int chunkZ, final boolean save) {
        return this.generateOrGetCell(Math.floorDiv(chunkX, CELL_SIDE_CHUNK_LENGTH),
                Math.floorDiv(chunkZ, CELL_SIDE_CHUNK_LENGTH),
                save);
    }

    private @NotNull BellSanctuaryCell generateOrGetCell(final int cellX, final int cellZ, final boolean save) {
        BellSanctuaryCell sanctuaryCell = this.bellSanctuaryCells.get(cellX, cellZ);

        if (sanctuaryCell == null) {
            sanctuaryCell = new BellSanctuaryCell(cellX, cellZ);
            this.bellSanctuaryCells.put(cellX, cellZ, sanctuaryCell);

            if (save) {
                this.setDirty();
            }
        }

        return sanctuaryCell;
    }

    @ApiStatus.Internal
    public void clean() {
        this.bellSanctuaryCells.values().removeIf(BellSanctuaryCell::clean);
    }

    @ApiStatus.Internal
    public BellSanctuaryGrid deserialize(final CompoundTag data, final HolderLookup.Provider prov) {
        this.clean(); //clear data and then repopulate

        final long[] sanctuaries = data.getLongArray("sanctuaries");
        assert sanctuaries.length % 2 == 0 : "Sanctuary data is not an even array: " + sanctuaries.length + " . Something is wrong!";
        for (int i = 0; i < sanctuaries.length; i++) {
            if (i % 2 == 1) { //skip every odd
                continue;
            }

            final long first = sanctuaries[i];
            final long second = sanctuaries[i + 1];

            final ChunkPos firstPos = new ChunkPos(first);
            final ChunkPos secondPos = new ChunkPos(second);
            final BellSanctuaryCell.SanctuaryPair pair = new BellSanctuaryCell.SanctuaryPair(firstPos, secondPos);

            final BellSanctuaryCell firstCell = this.generateOrGetCellChunkPos(firstPos.x, firstPos.z, false);
            final BellSanctuaryCell secondCell = this.generateOrGetCellChunkPos(secondPos.x, secondPos.z, false);

            firstCell.addPair(pair);
            if (firstCell != secondCell) {
                secondCell.addPair(pair);
            }
        }

        return this;
    }

    @Override
    @ApiStatus.Internal
    public CompoundTag save(final CompoundTag tag, final HolderLookup.Provider registries) {
        final LongSet longs = new LongArraySet();
        for (final BellSanctuaryCell cell : this.bellSanctuaryCells.values()) {
            for (final BellSanctuaryCell.SanctuaryPair pair : cell) {
                longs.add(pair.first().toLong());
                longs.add(pair.second().toLong());
            }
        }

        tag.put("sanctuaries", new LongArrayTag(longs));
        return tag;
    }
}
