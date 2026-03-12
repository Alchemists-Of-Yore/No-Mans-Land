package com.farcr.nomansland.common.handler.sanctuary_grid;

import com.farcr.nomansland.common.world.structure.SanctuaryRuinsStructurePlacement;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class SanctuaryGridHandler {

    private static final Long2ObjectMap<SanctuaryGrid> LEVEL_SEED_MAP = new Long2ObjectOpenHashMap<>();

    //called when server level is created
    public static void populateOrCreateData(final ServerLevel level) {
        level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                () -> LEVEL_SEED_MAP.computeIfAbsent(level.getSeed(), SanctuaryGrid::new),
                (tag, prov) -> LEVEL_SEED_MAP.computeIfAbsent(level.getSeed(), SanctuaryGrid::new).deserialize(tag, prov)
        ), "sanctuary_ruins");
    }

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
    public static SanctuaryCell generateOrGetCell(final ChunkGeneratorStructureState state, final SanctuaryRuinsStructurePlacement placement, final int blockX, final int blockZ) {
        return LEVEL_SEED_MAP.computeIfAbsent(state.getLevelSeed(), SanctuaryGrid::new)
                .generateOrGetCell(state, placement, blockX, blockZ);
    }

    public static void removeGrid(final long level) {
        final SanctuaryGrid removed = LEVEL_SEED_MAP.remove(level);

        if (removed != null) {
            removed.clean();
        }
    }
}
