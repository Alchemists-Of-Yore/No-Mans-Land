package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.extension.MinecraftServerExtension;
import com.farcr.nomansland.common.world.densityfunction.modification.DensityFunctionModifications;
import com.farcr.nomansland.common.world.densityfunction.modification.DensityFunctionModifier;
import com.farcr.nomansland.common.world.densityfunction.modification.NoiseRouterParameter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements MinecraftServerExtension {

    @Shadow public abstract ServerLevel overworld();
    @Shadow @Final private Map<ResourceKey<Level>, ServerLevel> levels;
    @Shadow @Final private Executor executor;
    @Shadow @Final private ChunkProgressListenerFactory progressListenerFactory;
    @Shadow @Final protected WorldData worldData;
    @Shadow @Final protected LevelStorageSource.LevelStorageAccess storageSource;

    @Override
    public Map<ResourceKey<Level>, ServerLevel> nml$getLevelList() {
        return this.levels;
    }

    @Override
    public Executor nml$getExecutor() {
        return executor;
    }

    @Override
    public LevelStorageSource.LevelStorageAccess nml$getLevelStorageAccess() {
        return storageSource;
    }

    @Override
    public ChunkProgressListener nml$getProgressListener() {
        return this.progressListenerFactory.create(
            this.worldData.getGameRules().getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS)
        );
    }

    @WrapOperation(method = "createLevels", at = @At(
                    value = "NEW",
                    target = "net/minecraft/server/level/ServerLevel"
            )
    )
    private ServerLevel nml$modifyNoiseRouter(
            MinecraftServer server,
            Executor workerExecutor,
            LevelStorageSource.LevelStorageAccess session,
            ServerLevelData properties,
            ResourceKey<Level> worldKey,
            LevelStem dimensionOptions,
            ChunkProgressListener worldGenerationProgressListener,
            boolean debugWorld,
            long seed,
            List<CustomSpawner> spawners,
            boolean shouldTickTime,
            @Nullable RandomSequences randomSequencesState,
            Operation<ServerLevel> operation) {

        Optional<ResourceKey<DimensionType>> dimensionKey = dimensionOptions.type().unwrapKey();

        if (dimensionKey.isPresent() && DensityFunctionModifications.NOISE_ROUTER_MODIFICATIONS.containsKey(dimensionKey.get())) {
            DensityFunctionModifications.NoiseRouterModifications noiseRouterModifications = DensityFunctionModifications.NOISE_ROUTER_MODIFICATIONS.get(dimensionKey.get());

            ChunkGenerator chunkGenerator = dimensionOptions.generator();
            if (chunkGenerator instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator) {
                NoiseGeneratorSettings noiseGeneratorSettings = noiseBasedChunkGenerator.generatorSettings().value();
                NoiseRouter noiseRouter = noiseGeneratorSettings.noiseRouter();

                for (Map.Entry<NoiseRouterParameter, DensityFunctionModifier> entry : noiseRouterModifications.modifiers.entrySet()) {
                    entry.getKey().modify(noiseRouter, entry.getValue());
                }
            }
        }

        return operation.call(server, workerExecutor, session, properties, worldKey, dimensionOptions, worldGenerationProgressListener, debugWorld, seed, spawners, shouldTickTime, randomSequencesState);
    }
}
