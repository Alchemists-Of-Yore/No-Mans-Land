package com.farcr.nomansland.common.extension;

import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

public interface ChunkGeneratorExtension {
    default @Nullable ChunkGeneratorStructureState nomansland$structureState() throws NotImplementedException {
        throw new NotImplementedException();
    }

    default void nomansland$setStructureState(ChunkGeneratorStructureState state) {
    }
}
