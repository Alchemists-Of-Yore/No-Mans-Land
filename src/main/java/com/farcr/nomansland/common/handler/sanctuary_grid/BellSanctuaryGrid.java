package com.farcr.nomansland.common.handler.sanctuary_grid;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A grid of {@link BellSanctuaryCell cells}. Each cell contains A pair of Bell Sanctuary {@link ChunkPos section positions.}
 */
public class BellSanctuaryGrid extends SavedData {

    /**
     * How many chunks long and tall a single cell is
     */
    public static final int CELL_SIDE_CHUNK_LENGTH = 500;

    /**
     * the block equivalent of {@link BellSanctuaryGrid#CELL_SIDE_CHUNK_LENGTH}
     */
    public static final int CELL_SIDE_BLOCK_LENGTH = CELL_SIDE_CHUNK_LENGTH * 16;

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
     * Attempts to generate a {@link BellSanctuaryCell cell} given the X and Z block positions.
     *
     * @return A newly generated {@link BellSanctuaryCell cell}, or an already present one.
     */
    @NotNull
    public BellSanctuaryCell generateOrGetCell(final int blockX, final int blockZ) {
        final int cellX = Math.floorDiv(blockX, CELL_SIDE_BLOCK_LENGTH); // should keep sign
        final int cellZ = Math.floorDiv(blockZ, CELL_SIDE_BLOCK_LENGTH); // should keep sign

        BellSanctuaryCell sanctuaryCell = this.bellSanctuaryCells.get(cellX, cellZ);
        if (sanctuaryCell == null) {
            sanctuaryCell = new BellSanctuaryCell(cellX, cellZ);
            this.bellSanctuaryCells.put(cellX, cellZ, new BellSanctuaryCell(cellX, cellZ));
        }

        if (!sanctuaryCell.hasAttemptedToGenerate()) {
            final BellSanctuaryCell[][] adjacent = new BellSanctuaryCell[3][3];

            //-1 -> 1
            for (int adjX = -1; adjX < 2; adjX++) {
                for (int adjZ = -1; adjZ < 2; adjZ++) {
                    //center cell
                    if (adjX == 0 && adjZ == 0) {
                        continue;
                    }

                    //+1 to avoid negative indices inside array
                    adjacent[adjX + 1][adjZ + 1] = this.bellSanctuaryCells.get(cellX + adjX, cellZ + adjZ);
                }
            }

            sanctuaryCell.generatePositionsNoBiome(this.levelSeed);
        }

        this.setDirty();
        return sanctuaryCell;
    }

    /**
     * Attempts to get the closest sanctuary from the given block position. <p>
     *     Searches in a 3x3 area of cells.
     *
     * @param blockPos the block position to center the search on.
     * @return The chunk position of the nearest sanctuary, null if none were found.
     */
    @Nullable
    public ChunkPos getClosestBellSanctuary3x3(final BlockPos blockPos) {
        final int cellX = Math.floorDiv(blockPos.getX(), CELL_SIDE_BLOCK_LENGTH);
        final int cellZ = Math.floorDiv(blockPos.getZ(), CELL_SIDE_BLOCK_LENGTH);

        ChunkPos closestChunk = null;
        double closestDistanceSqr = 0;

        final BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos();
        //3x3 centered on given grid
        for (int localX = -1; localX < 2; localX++) {
            for (int localZ = -1; localZ < 2; localZ++) {
                final BellSanctuaryCell cell = this.bellSanctuaryCells.get(cellX + localX, cellZ + localZ);

                if (cell != null && cell.isValid()) {
                    for (final ChunkPos sanctuaryPos : cell) {
                        mut.set(sanctuaryPos.getBlockX(8), blockPos.getY(), sanctuaryPos.getBlockZ(8));
                        if (closestChunk == null) {
                            closestChunk = sanctuaryPos;
                            closestDistanceSqr = mut.distSqr(blockPos);
                            continue;
                        }

                        final double testDistanceSqr = mut.distSqr(blockPos);
                        if (testDistanceSqr < closestDistanceSqr) {
                            closestDistanceSqr = testDistanceSqr;
                            closestChunk = sanctuaryPos;
                        }
                    }
                }
            }
        }

        return closestChunk;
    }

    @ApiStatus.Internal
    public void clean() {
        this.bellSanctuaryCells.clear();
    }

    @ApiStatus.Internal
    public BellSanctuaryGrid deserialize(final CompoundTag data, final HolderLookup.Provider prov) {
        this.clean(); //clear data and then repopulate

        if (data.get("cells") instanceof final ListTag lt) {
            for (final Tag tag : lt) {
                final CompoundTag cellData = (CompoundTag) tag;
                final BellSanctuaryCell cell = BellSanctuaryCell.deserialize(cellData);

                this.bellSanctuaryCells.put(cell.x, cell.z, cell);
            }
        }

        return this;
    }

    @Override
    @ApiStatus.Internal
    public CompoundTag save(final CompoundTag tag, final HolderLookup.Provider registries) {
        final ListTag gridTag = new ListTag();
        for (final BellSanctuaryCell value : this.bellSanctuaryCells.values()) {
            gridTag.add(value.serialize());
        }

        tag.put("cells", gridTag);
        return tag;
    }
}
