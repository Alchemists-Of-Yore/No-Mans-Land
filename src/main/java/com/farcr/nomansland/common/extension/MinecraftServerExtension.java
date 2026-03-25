package com.farcr.nomansland.common.extension;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Map;
import java.util.concurrent.Executor;

public interface MinecraftServerExtension {
    default Map<ResourceKey<Level>, ServerLevel> nml$getLevelList() throws NotImplementedException {
        throw new NotImplementedException();
    }
    default Executor nml$getExecutor() throws NotImplementedException {
        throw new NotImplementedException();
    }
    default LevelStorageSource.LevelStorageAccess nml$getLevelStorageAccess() throws NotImplementedException {
        throw new NotImplementedException();
    }
    default ChunkProgressListener nml$getProgressListener() throws NotImplementedException {
        throw new NotImplementedException();
    }
}
