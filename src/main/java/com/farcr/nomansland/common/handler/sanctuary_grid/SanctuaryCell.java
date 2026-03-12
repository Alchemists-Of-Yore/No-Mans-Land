package com.farcr.nomansland.common.handler.sanctuary_grid;

import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;

//wowee wow weeee
public class SanctuaryCell {

    public static final int MIN_DISTANCE = 1_000;
    public static final int MAX_DISTANCE = 6_000;

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
    public void generatePositions(final SanctuaryCell[][] adjacentCells) {

    }

    public boolean hasAttemptedToGenerate() {
        return this.attemptedToGenerate;
    }

    public boolean valid() {
        return this.isValid;
    }
}
