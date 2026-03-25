package com.farcr.nomansland.common.handler.sanctuary_grid;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.world.structure.bell_sanctuary.BellSanctuaryStructurePlacement;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Static handler for {@link ServerLevel#getSeed() server level seeds} -> {@link BellSanctuaryGrid grids}
 */
public class BellSanctuaryGridHandler {

    private static final Long2ObjectMap<BellSanctuaryGrid> LEVEL_SEED_MAP = new Long2ObjectOpenHashMap<>();

    /**
     * Populates {@link BellSanctuaryGridHandler#LEVEL_SEED_MAP} With the given {@link ServerLevel}'s saved data. <p>
     * Either creates, or gets the current BellSanctuary saved data, and loads the correct entry.
     */
    @ApiStatus.Internal
    public static void populateOrCreateData(final ServerLevel level) {
        level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                () -> LEVEL_SEED_MAP.computeIfAbsent(level.getSeed(), BellSanctuaryGrid::new),
                (tag, prov) -> LEVEL_SEED_MAP.computeIfAbsent(level.getSeed(), BellSanctuaryGrid::new).deserialize(tag, prov)
        ), "bell_sanctuaries");
    }

    /**
     * Attempts to get the {@link BellSanctuaryCell cell} associated with the given X and Z block positions.
     *
     * @return The associated {@link BellSanctuaryCell cell}, or null if none exists.
     */
    @Nullable
    public static BellSanctuaryCell getCell(final long serverLevelSeed, final int blockX, final int blockZ) {
        //TODO: profile?
        return LEVEL_SEED_MAP.computeIfAbsent(serverLevelSeed, BellSanctuaryGrid::new)
                .getCell(blockX, blockZ);
    }

    /**
     * Attempts to get the {@link BellSanctuaryGrid grid} associated with the given {@link ServerLevel#getSeed() server level seed}.
     *
     * @return The associated {@link BellSanctuaryGrid grid}
     */
    @NotNull
    public static BellSanctuaryGrid getGrid(final long serverLevelSeed) {
        return LEVEL_SEED_MAP.computeIfAbsent(serverLevelSeed, BellSanctuaryGrid::new);
    }

    @ApiStatus.Internal
    public static boolean tryGeneratePair(final long levelSeed, BellSanctuaryStructurePlacement placement, ChunkPos pos) {
        return LEVEL_SEED_MAP.computeIfAbsent(levelSeed, BellSanctuaryGrid::new).tryGeneratePair(pos, placement);
    }

    @ApiStatus.Internal
    public static void clean() {
        LEVEL_SEED_MAP.clear();
    }

    /**
     * How many chunks long and tall a single cell is
     */
    public static Integer getCellSideChunkLength() {
        return NMLConfig.BELL_CELL_SIZE_CHUNKS.getAsInt();
    }

    /**
     * The minimum distance allowed between two Bell Sanctuary position in a pair.
     */
    public static Integer getMinChunkDistance() {
        return NMLConfig.MIN_BELL_SANCTUARY_PAIR_DISTANCE_CHUNKS.getAsInt();
    }

    /**
     * The maximum distance allowed between two Bell Sanctuary positions in a pair.
     */
    public static Integer getMaxChunkDistance() {
        return NMLConfig.MAX_BELL_SANCTUARY_PAIR_DISTANCE_CHUNKS.getAsInt();
    }
}
