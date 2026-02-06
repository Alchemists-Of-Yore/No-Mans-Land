package com.farcr.nomansland.common.extension;

import jdk.jshell.spi.ExecutionControl;
import net.minecraft.world.level.ChunkPos;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.Nullable;

public interface ChunkGeneratorStructureStateExtension {
    default @Nullable ChunkPos meetingPointPosition() throws NotImplementedException {
        throw new NotImplementedException();
    };
}
