package com.farcr.nomansland.common.handler.sanctuary_grid;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
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
        ), "bell_sanctuarys");
    }

    /**
     * Attempts to get the {@link BellSanctuaryCell cell} associated with the given X and Z block positions.
     *
     * @return The associated {@link BellSanctuaryCell cell}, or null if none exists.
     */
    @Nullable
    public static BellSanctuaryCell getCell(final long serverLevelSeed, final int blockX, final int blockZ) {
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

    /**
     * Attempts to generate the {@link BellSanctuaryCell cell} for the given X and Z position. If a cell already exists, does nothing.
     */
    @ApiStatus.Internal
    public static void generateCell(final long levelSeed, final int chunkX, final int chunkZ) {
        LEVEL_SEED_MAP.computeIfAbsent(levelSeed, BellSanctuaryGrid::new).generateCell(chunkX, chunkZ);
    }

    @ApiStatus.Internal
    public static void clean() {
        LEVEL_SEED_MAP.clear();
    }
}
