package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.densityfunction.modification.DensityFunctionModifications;
import com.farcr.nomansland.common.world.densityfunction.modification.DensityFunctionModifier;
import com.farcr.nomansland.common.world.densityfunction.modification.NoiseRouterParameter;
import com.farcr.nomansland.common.world.watershed.WatershedDensityFunctionVisitor;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
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
                HolderGetter<NormalNoise.NoiseParameters> noiseParamsRegistry = server.registryAccess().lookupOrThrow(Registries.NOISE);
                HolderGetter<DensityFunction> densityFuncRegistry = server.registryAccess().lookupOrThrow(Registries.DENSITY_FUNCTION);
                // i hate this
                ((NoiseGeneratorSettingsAccessor)(Object)noiseGeneratorSettings).nml$setNoiseRouter(
                        new NoiseRouter(
                            NoiseRouterParameter.BARRIER_NOISE.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.FLUID_LEVEL_FLOODEDNESS_NOISE.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.FLUID_LEVEL_SPREAD_NOISE.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.LAVA_NOISE.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.TEMPERATURE.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.VEGETATION.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.CONTINENTS.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.EROSION.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.DEPTH.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.RIDGES.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.INITIAL_DENSITY_WITHOUT_JAGGEDNESS.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.FINAL_DENSITY.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.VEIN_TOGGLE.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.VEIN_RIDGED.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry),
                            NoiseRouterParameter.VEIN_GAP.maybeModify(noiseRouter, noiseRouterModifications, noiseParamsRegistry, densityFuncRegistry)
                        )
                );

                NoMansLand.LOGGER.info(noiseGeneratorSettings.noiseRouter());
            }
        }

        return operation.call(server, workerExecutor, session, properties, worldKey, dimensionOptions, worldGenerationProgressListener, debugWorld, seed, spawners, shouldTickTime, randomSequencesState);
    }
}
