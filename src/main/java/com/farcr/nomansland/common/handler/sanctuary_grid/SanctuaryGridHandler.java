package com.farcr.nomansland.common.handler.sanctuary_grid;

import com.farcr.nomansland.common.world.structure.SanctuaryRuinsStructurePlacement;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class SanctuaryGridHandler {

    private static final Long2ObjectMap<SanctuaryGrid> LEVEL_SEED_MAP = new Long2ObjectOpenHashMap<>();

    @Nullable
    public static SanctuaryCell getCell(final long level, final int blockX, final int blockZ) {
        return LEVEL_SEED_MAP.computeIfAbsent(level, SanctuaryGrid::new)
                .getCell(blockX, blockZ);
    }

    @NotNull
    public static SanctuaryGrid getGrid(final long level) {
        return LEVEL_SEED_MAP.computeIfAbsent(level, SanctuaryGrid::new);
    }

    /**
     * Attempts to generate the cell for the given X and Z position.
     *
     * @param state The chunk generation state.
     * @return The newly generated cell, or an already present one.
     */
    @NotNull
    public static SanctuaryCell generateOrGetCell(final ChunkGeneratorStructureState state, SanctuaryRuinsStructurePlacement placement, final int blockX, final int blockZ) {
        return LEVEL_SEED_MAP.computeIfAbsent(state.getLevelSeed(), SanctuaryGrid::new)
                .generateOrGetCell(state, placement, blockX, blockZ);
    }

    public static void removeGrid(final long level) {
        final SanctuaryGrid removed = LEVEL_SEED_MAP.remove(level);

        if (removed != null) {
            LEVEL_SEED_MAP.remove(level).clean();
        }
    }


}
