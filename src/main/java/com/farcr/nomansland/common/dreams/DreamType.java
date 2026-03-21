package com.farcr.nomansland.common.dreams;

import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

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

    protected void defaultChunkGenerator(ChunkAccess chunk) {
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

    public Consumer<ChunkAccess> chunkGenerator = this::defaultChunkGenerator;
    public DreamType setChunkGenerator(Consumer<ChunkAccess> chunkGenerator) {
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

    public static class DreamTypeInstance {
        public DreamTypeInstance(DreamType dreamType) {
            this.dreamType = dreamType;
        }
        public DreamType dreamType;
    }
}
