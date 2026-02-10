package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.orevein.OreVeinSystem;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {

    @Shadow @Final private Holder<NoiseGeneratorSettings> settings;
    @Unique OreVeinSystem nml$oreVeinSystem;
    @Unique
    OreVeinSystem nml$getOreVeinSystem(WorldGenLevel level) {
        if (nml$oreVeinSystem == null) nml$oreVeinSystem = new OreVeinSystem(level);
        return nml$oreVeinSystem;
    }

    @Inject(
            method = "buildSurface(Lnet/minecraft/server/level/WorldGenRegion;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/levelgen/NoiseBasedChunkGenerator;buildSurface(Lnet/minecraft/world/level/chunk/ChunkAccess;Lnet/minecraft/world/level/levelgen/WorldGenerationContext;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/biome/BiomeManager;Lnet/minecraft/core/Registry;Lnet/minecraft/world/level/levelgen/blending/Blender;)V"
            )
    )
    void nml$buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk, CallbackInfo ci, @Local WorldGenerationContext context) {
        OreVeinSystem system = nml$getOreVeinSystem(level);
        system.buildVeins(chunk, context, random, this.settings.value().defaultBlock());
    }
}
