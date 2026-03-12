package com.farcr.nomansland.common.handler.sanctuary_grid;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SanctuaryGridHandler {

    private static final Long2ObjectMap<SanctuaryGrid> LEVEL_SEED_MAP = new Long2ObjectOpenHashMap<>();

    @Nullable
    public static SanctuaryCell getCell(long level, int blockX, int blockZ) {
        return LEVEL_SEED_MAP.computeIfAbsent(level, l -> new SanctuaryGrid())
                .getCell(blockX, blockZ);
    }

    @NotNull
    public static SanctuaryGrid getGrid(final long level) {
        return LEVEL_SEED_MAP.computeIfAbsent(level, l -> new SanctuaryGrid());
    }

    public static void removeGrid(final long level) {
        final SanctuaryGrid removed = LEVEL_SEED_MAP.remove(level);

        if (removed != null) {
            LEVEL_SEED_MAP.remove(level).clean();
        }
    }


}
