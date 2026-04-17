package com.farcr.nomansland.common.extension;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

public interface ChunkGeneratorStructureStateExtension {
    default @Nullable ChunkPos meetingPointPosition() throws NotImplementedException {
        throw new NotImplementedException();
    };

    default @Nullable ChunkGenerator nomansland$chunkGenerator() {
        return null;
    }

    default void nomansland$setChunkGenerator(ChunkGenerator generator) {
    }
}
