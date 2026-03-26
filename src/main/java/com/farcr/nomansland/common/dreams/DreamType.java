package com.farcr.nomansland.common.dreams;

import com.farcr.nomansland.client.renderer.dreams.IDreamRenderer;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.mehvahdjukaar.moonlight.api.misc.QuadConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.function.*;

/*
* Class that stores information about dream types`
*/
public class DreamType {

    public static final Codec<DreamType> CODEC = NMLRegistries.DREAM_TYPE.byNameCodec();

    public final @Nullable BiFunction<ServerPlayer, ServerLevel, Boolean> biconsumer;
    public DreamType(@Nullable BiFunction<ServerPlayer, ServerLevel, Boolean> condition) {
        this.biconsumer = condition;
    }

    public boolean canSprint = false;
    public DreamType setCanSprint(boolean canSprint) {
        this.canSprint = canSprint;
        return this;
    }

    protected void defaultChunkGenerator(ChunkAccess chunk, StructureManager manager, WorldGenRegion level) {
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                BlockPos blockPos = new BlockPos(i, 0, j);
                chunk.setBlockState(
                    blockPos,
                    Blocks.BEDROCK.defaultBlockState(),
                    false
                );
                if (i % 8 == 0 && j % 8 == 0) {
                    chunk.setBlockState(
                        blockPos.above(),
                        Blocks.GLOWSTONE.defaultBlockState(),
                        false
                    );
                }
            }
        }
    }

    public void createStructures(
        ChunkGenerator generator,
        RegistryAccess registryAccess,
        ChunkGeneratorStructureState structureState,
        StructureManager structureManager, ChunkAccess chunk,
        StructureTemplateManager structureTemplateManager
    ) {

    }

    public Supplier<IDreamRenderer> dreamRenderer;
    public DreamType setRenderer(Supplier<IDreamRenderer> dreamRenderer) {
        this.dreamRenderer = dreamRenderer;
        return this;
    }

    public void tick(Level level) {}

    public TriConsumer<ChunkAccess, StructureManager, WorldGenRegion> chunkGenerator = this::defaultChunkGenerator;
    public DreamType setChunkGenerator(TriConsumer<ChunkAccess, StructureManager, WorldGenRegion> chunkGenerator) {
        this.chunkGenerator = chunkGenerator;
        return this;
    }

    public Vec3 spawnPoint = new Vec3(0, 0, 0);
    public DreamType setSpawnPoint(Vec3 spawnPoint) {
        this.spawnPoint = spawnPoint;
        return this;
    }

    public double worldBorder = 0.0d;
    public DreamType setWorldBorder(double worldBorder) {
        this.worldBorder = worldBorder;
        return this;
    }

    private boolean hideHUD = true;
    public boolean hideHUD() {
        return hideHUD;
    }

    public DreamType setHUDHidden(boolean hudHidden) {
        this.hideHUD = hudHidden;
        return this;
    }
}
