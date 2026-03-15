package com.farcr.nomansland.common.handler.sanctuary_grid;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BellSanctuaryCell implements Iterable<BellSanctuaryCell.SanctuaryPair> {

    private final CellPos pos;
    private final Map<ChunkPos, SanctuaryPair> trackedPairs = new Object2ObjectOpenHashMap<>();

    public BellSanctuaryCell(final int x, final int z) {
        this.pos = new CellPos(x, z);
    }

    public boolean containsPosition(final ChunkPos toCheck) {
        return this.trackedPairs.containsKey(toCheck);
    }

    @Override
    public @NotNull Iterator<SanctuaryPair> iterator() {
        return this.trackedPairs.values().iterator();
    }

    public void addPair(final SanctuaryPair newPair) {
        this.trackedPairs.put(newPair.first, newPair);
        this.trackedPairs.put(newPair.second, newPair);
    }

    public boolean clean() {
        this.trackedPairs.clear();
        return true;
    }

    public record SanctuaryPair(ChunkPos first, ChunkPos second) {

    }

    public record CellPos(int x, int z) {

    }
}
