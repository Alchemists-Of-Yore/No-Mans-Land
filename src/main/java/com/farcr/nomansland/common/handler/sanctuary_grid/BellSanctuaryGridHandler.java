package com.farcr.nomansland.common.handler.sanctuary_grid;

import com.farcr.nomansland.common.world.structure.BellSanctuaryStructurePlacement;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BellSanctuaryGridHandler {

    private static final Long2ObjectMap<BellSanctuaryGrid> LEVEL_SEED_MAP = new Long2ObjectOpenHashMap<>();

    //called when server level is created
    public static void populateOrCreateData(final ServerLevel level) {
        level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                () -> LEVEL_SEED_MAP.computeIfAbsent(level.getSeed(), BellSanctuaryGrid::new),
                (tag, prov) -> LEVEL_SEED_MAP.computeIfAbsent(level.getSeed(), BellSanctuaryGrid::new).deserialize(tag, prov)
        ), "bell_sanctuarys");
    }

    @Nullable
    public static BellSanctuaryCell getCell(final long level, final int blockX, final int blockZ) {
        return LEVEL_SEED_MAP.computeIfAbsent(level, BellSanctuaryGrid::new)
                .getCell(blockX, blockZ);
    }

    @NotNull
    public static BellSanctuaryGrid getGrid(final long level) {
        return LEVEL_SEED_MAP.computeIfAbsent(level, BellSanctuaryGrid::new);
    }

    /**
     * Attempts to generate the cell for the given X and Z position.
     *
     * @param state The chunk generation state.
     * @return The newly generated cell, or an already present one.
     */
    @NotNull
    public static BellSanctuaryCell generateOrGetCell(final ChunkGeneratorStructureState state, final BellSanctuaryStructurePlacement placement, final int blockX, final int blockZ) {
        return LEVEL_SEED_MAP.computeIfAbsent(state.getLevelSeed(), BellSanctuaryGrid::new)
                .generateOrGetCell(state, placement, blockX, blockZ);
    }

    public static void clean() {
        LEVEL_SEED_MAP.clear();
    }
}
