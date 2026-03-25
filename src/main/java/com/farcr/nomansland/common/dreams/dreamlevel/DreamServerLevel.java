package com.farcr.nomansland.common.dreams.dreamlevel;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executor;

public class DreamServerLevel extends ServerLevel {
    public DreamServerLevel(
        MinecraftServer server, Executor dispatcher,
        LevelStorageSource.LevelStorageAccess levelStorageAccess,
        ServerLevelData serverLevelData, ResourceKey<Level> dimension,
        LevelStem levelStem, ChunkProgressListener progressListener,
        boolean isDebug, long biomeZoomSeed, List<CustomSpawner> customSpawners,
        boolean tickTime, @Nullable RandomSequences randomSequences
    ) {
        super(
            server, dispatcher,
            levelStorageAccess,
            serverLevelData,
            dimension, levelStem,
            progressListener, isDebug,
            biomeZoomSeed, customSpawners,
            tickTime, randomSequences
        );
    }

    @Override public void tickPrecipitation(BlockPos blockPos) {}

    @Override public boolean mayInteract(Player player, BlockPos pos) { return false; }

    @Override public void save(@Nullable ProgressListener progress, boolean flush, boolean skipSave) {}
}
