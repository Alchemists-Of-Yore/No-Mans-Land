package com.farcr.nomansland.common.dreams.dreamlevel;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.extension.MinecraftServerExtension;
import com.farcr.nomansland.common.networking.dream.ClientboundDimensionSyncPacket;
import com.farcr.nomansland.common.registry.NMLDreamTypes;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.WorldData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.Executor;

public class DreamLevelHandler implements AutoCloseable {
    private static DreamLevelHandler INSTANCE;
    public static DreamLevelHandler getInstance() {
        if (INSTANCE == null)
            INSTANCE = new DreamLevelHandler();
        return INSTANCE;
    }

    public static void destroy() {
        INSTANCE.close();
        INSTANCE = null;
    }

    @Override
    public void close() {}

    public static <T> ResourceKey<T> resourceKey(ResourceKey<? extends Registry<T>> resourceKey, ResourceLocation dreamLocation, Player player) {
        return ResourceKey.create(resourceKey,
            ResourceLocation.fromNamespaceAndPath(
                dreamLocation.getNamespace(),
                dreamLocation.getPath() + "_" + player.getStringUUID()
            )
        );
    }

    public static ServerLevel getDreamLevel(MinecraftServer server, DreamType dreamType, ServerPlayer serverPlayer) {
        ResourceLocation dreamLocation = NMLDreamTypes.DREAM_TYPES_REGISTRY.getRegistry().get().getKey(dreamType);
        ResourceKey<Level> dreamKey = resourceKey(Registries.DIMENSION, dreamLocation, serverPlayer);

        Map<ResourceKey<Level>, ServerLevel> levelList = ((MinecraftServerExtension) server).nml$getLevelList();
        Executor executor = ((MinecraftServerExtension) server).nml$getExecutor();
        LevelStorageAccess storageAccess = ((MinecraftServerExtension) server).nml$getLevelStorageAccess();

        if (!levelList.containsKey(dreamKey)) {
            ServerLevel overworld = server.overworld();
            BiomeSource biomeSource = new FixedBiomeSource(
                server.registryAccess().registryOrThrow(Registries.BIOME)
                    .getHolderOrThrow(NMLBiomes.DREAM)
            );

            WorldData worldData = server.getWorldData();
            ChunkProgressListener chunkprogresslistener = ((MinecraftServerExtension) server).nml$getProgressListener();
            Holder<DimensionType> dreamHolder = registerDimensionType(server.registryAccess(), dreamType, serverPlayer);

            ServerLevel newLevel = new DreamServerLevel(
                server, executor, storageAccess,
                new DerivedLevelData(worldData, worldData.overworldData()),
                dreamKey, new LevelStem(
                    dreamHolder, new DreamChunkGenerator(dreamType, biomeSource)
                ),
                chunkprogresslistener, worldData.isDebugWorld(),
                overworld.getSeed(), List.of(), false,
                overworld.getRandomSequences()
            );

            if (dreamType.worldBorder > 0) newLevel.getWorldBorder().setSize(dreamType.worldBorder);

            levelList.put(dreamKey, newLevel);

            // not entirely trustworthy
            // but https://github.com/Commoble/infiniverse/blob/main/src/main/java/net/commoble/infiniverse/internal/InfiniverseMod.java
            server.markWorldsDirty();

            // REMEMBER to tell players what the new dimension set is
            PacketDistributor.sendToAllPlayers(
                new ClientboundDimensionSyncPacket(server.levelKeys()));
        }
        return levelList.get(dreamKey);
    }

    /*
    * Has to be separate, in case dreams need any of the dimensiontype
    * parameters as their own / to be modifiable !!!
    *
    * also has to be separate to register on both client and server !!!
    */
    public static Holder<DimensionType> registerDimensionType(RegistryAccess registryAccess, DreamType dreamType, Player player) {
        Registry<DimensionType> dimensionRegistry =
            registryAccess.registryOrThrow(Registries.DIMENSION_TYPE);

        DimensionType dimensionType = new DimensionType(
            OptionalLong.of(18000),
            false, false, false, true,
            1d, false, false, 0,
            128, 128, BlockTags.INFINIBURN_OVERWORLD,
            BuiltinDimensionTypes.OVERWORLD_EFFECTS, 0.0F,
            new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)
        );

        ResourceLocation dreamLocation = NMLDreamTypes.DREAM_TYPES_REGISTRY.getRegistry().get().getKey(dreamType);
        ResourceKey<DimensionType> dimensionKey = resourceKey(Registries.DIMENSION_TYPE, dreamLocation, player);
        if (!dimensionRegistry.containsKey(dimensionKey) && dimensionRegistry instanceof MappedRegistry<DimensionType> writableRegistry) {
            writableRegistry.unfreeze();
            writableRegistry.register(dimensionKey, dimensionType,
                new RegistrationInfo(Optional.empty(), Lifecycle.stable())
            );
        }
        return dimensionRegistry.getHolderOrThrow(dimensionKey);
    }
}
