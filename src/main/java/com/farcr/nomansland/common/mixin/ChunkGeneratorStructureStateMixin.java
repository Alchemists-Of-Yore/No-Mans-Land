package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.extension.ChunkGeneratorStructureStateExtension;
import com.farcr.nomansland.common.world.structure.MeetingPointStructurePlacement;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkGeneratorStructureState.class)
public abstract class ChunkGeneratorStructureStateMixin implements ChunkGeneratorStructureStateExtension {
    @Shadow
    @Final
    private long levelSeed;

    @Shadow
    @Final
    private BiomeSource biomeSource;

    @Shadow
    @Final
    private RandomState randomState;

    @Shadow
    public abstract void ensureStructuresGenerated();

    @Unique
    private CompletableFuture<ChunkPos> meetingPointPosition = null;

    @Unique
    private HolderSet<Biome> nomansland$preferredBiomes = null;

    @Unique
    private ChunkPos nomansland$meetingPointOverride = null;

    @Unique
    private ChunkGenerator nomansland$chunkGenerator = null;

    @Override
    public ChunkGenerator nomansland$chunkGenerator() {
        return nomansland$chunkGenerator;
    }

    @Override
    public void nomansland$setChunkGenerator(ChunkGenerator generator) {
        this.nomansland$chunkGenerator = generator;
    }

    @Inject(method = "lambda$generatePositions$4", at = @At("TAIL"))
    private void generateMeetingPointPosition(Set<Holder<StructureSet>> possibleStructureSets, Holder<StructureSet> setHolder, CallbackInfo ci, @Local boolean hasAnyPlaceableStructures) {
        if (hasAnyPlaceableStructures && setHolder.value().placement() instanceof MeetingPointStructurePlacement meetingPointPlacement) {
            if (nomansland$chunkGenerator == null) {
                nomansland$chunkGenerator = ChunkGeneratorStructureStateExtension.CURRENT_GENERATOR.get();
            }
            nomansland$preferredBiomes = meetingPointPlacement.preferredBiomes;
            meetingPointPosition = generateMeetingPointPosition(setHolder.value(), meetingPointPlacement);
        }
    }

    private CompletableFuture<ChunkPos> generateMeetingPointPosition(StructureSet structureSet, MeetingPointStructurePlacement placement) {
        HolderSet<Biome> preferredBiomes = placement.preferredBiomes;
        RandomSource random = RandomSource.create();
        random.setSeed(levelSeed);

        RandomSource biomeSearchGenerator = random.fork();
        return CompletableFuture.supplyAsync(
                () -> {
                    List<BlockPos> preferredCandidates = new ArrayList<>();
                    List<BlockPos> fallbackCandidates = new ArrayList<>();

                    for (int tries = 0; tries < 50; tries++) {
                        double angle = random.nextDouble() * Math.PI * 2.0;
                        double distance = random.nextInt(NMLConfig.MIN_MEETING_POINT_DISTANCE.get(), NMLConfig.MAX_MEETING_POINT_DISTANCE.get());
                        int x = (int) Math.round(Math.cos(angle) * distance);
                        int z = (int) Math.round(Math.sin(angle) * distance);

                        BlockPos rawPosition = new BlockPos(x, 0, z);
                        if (!isOverWater(rawPosition)) {
                            fallbackCandidates.add(rawPosition);
                        }

                        Pair<BlockPos, Holder<Biome>> candidate = findBiome(x, z, preferredBiomes, biomeSearchGenerator);
                        if (candidate != null && !isOverWater(candidate.getFirst())) {
                            preferredCandidates.add(candidate.getFirst());
                        }
                    }

                    BlockPos chosen;
                    if (!preferredCandidates.isEmpty()) {
                        chosen = flattestCandidate(preferredCandidates);
                    } else if (!fallbackCandidates.isEmpty()) {
                        chosen = flattestCandidate(fallbackCandidates);
                    } else {
                        double angle = random.nextDouble() * Math.PI * 2.0;
                        double distance = random.nextInt(NMLConfig.MIN_MEETING_POINT_DISTANCE.get(), NMLConfig.MAX_MEETING_POINT_DISTANCE.get());
                        chosen = new BlockPos((int) Math.round(Math.cos(angle) * distance), 0, (int) Math.round(Math.sin(angle) * distance));
                    }

                    return new ChunkPos(SectionPos.blockToSectionCoord(chosen.getX()), SectionPos.blockToSectionCoord(chosen.getZ()));
                }, Util.backgroundExecutor()
        );
    }

    @Unique
    private BlockPos flattestCandidate(List<BlockPos> candidates) {
        BlockPos flattest = candidates.get(0);
        int lowestRoughness = nomansland$terrainRoughness(flattest);
        for (int i = 1; i < candidates.size(); i++) {
            BlockPos candidate = candidates.get(i);
            int roughness = nomansland$terrainRoughness(candidate);
            if (roughness < lowestRoughness) {
                lowestRoughness = roughness;
                flattest = candidate;
            }
        }
        return flattest;
    }

    @Unique
    private int nomansland$terrainRoughness(BlockPos center) {
        ChunkGenerator generator = nomansland$chunkGenerator;
        if (generator == null) return Integer.MAX_VALUE;
        LevelHeightAccessor heightAccessor = LevelHeightAccessor.create(generator.getMinY(), generator.getGenDepth());
        int radius = 16;
        int step = 8;
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;
        for (int dx = -radius; dx <= radius; dx += step) {
            for (int dz = -radius; dz <= radius; dz += step) {
                int height = generator.getFirstOccupiedHeight(center.getX() + dx, center.getZ() + dz, Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
                if (height < minHeight) minHeight = height;
                if (height > maxHeight) maxHeight = height;
            }
        }
        return maxHeight - minHeight;
    }

    @Unique
    private boolean isOverWater(BlockPos position) {
        ChunkGenerator generator = nomansland$chunkGenerator;
        if (generator == null) return false;
        LevelHeightAccessor heightAccessor = LevelHeightAccessor.create(generator.getMinY(), generator.getGenDepth());
        int groundY = generator.getFirstOccupiedHeight(position.getX(), position.getZ(), Heightmap.Types.OCEAN_FLOOR_WG, heightAccessor, randomState);
        int surfaceY = generator.getFirstOccupiedHeight(position.getX(), position.getZ(), Heightmap.Types.WORLD_SURFACE_WG, heightAccessor, randomState);
        return surfaceY > groundY;
    }

    private Pair<BlockPos, Holder<Biome>> findBiome(int x, int z, HolderSet<Biome> preferredBiomes, RandomSource biomeSearchGenerator) {
        return biomeSource.findBiomeHorizontal(
                x,
                64,
                z,
                512,
                preferredBiomes::contains,
                biomeSearchGenerator,
                randomState.sampler()
        );
    }

    @Override
    public ChunkPos meetingPointPosition() {
        if (nomansland$meetingPointOverride != null) {
            return nomansland$meetingPointOverride;
        }
        ensureStructuresGenerated();
        return meetingPointPosition == null ? null : meetingPointPosition.join();
    }

    @Override
    public void nomansland$setMeetingPointPosition(ChunkPos pos) {
        this.nomansland$meetingPointOverride = pos;
    }

    @Override
    public ChunkPos nomansland$generatedMeetingPointPosition() {
        ensureStructuresGenerated();
        return meetingPointPosition == null ? null : meetingPointPosition.join();
    }

    @Override
    public ChunkPos nomansland$computeLegacyMeetingPointPosition() {
        ensureStructuresGenerated();
        if (nomansland$preferredBiomes == null) return null;

        RandomSource random = RandomSource.create();
        random.setSeed(levelSeed);
        RandomSource biomeSearchGenerator = random.fork();

        Pair<BlockPos, Holder<Biome>> closestBiome = null;
        int tries = 0;
        while (closestBiome == null && tries < 50) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = random.nextInt(NMLConfig.MIN_MEETING_POINT_DISTANCE.get(), NMLConfig.MAX_MEETING_POINT_DISTANCE.get());
            int x = (int) Math.round(Math.cos(angle) * distance);
            int z = (int) Math.round(Math.sin(angle) * distance);
            Pair<BlockPos, Holder<Biome>> candidate = findBiome(x, z, nomansland$preferredBiomes, biomeSearchGenerator);
            if (candidate != null && !isOverWater(candidate.getFirst())) {
                closestBiome = candidate;
            }
            tries++;
        }

        if (closestBiome == null) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = random.nextInt(NMLConfig.MIN_MEETING_POINT_DISTANCE.get(), NMLConfig.MAX_MEETING_POINT_DISTANCE.get());
            int x = (int) Math.round(Math.cos(angle) * distance);
            int z = (int) Math.round(Math.sin(angle) * distance);
            return new ChunkPos(new BlockPos(x, 0, z));
        } else {
            BlockPos position = closestBiome.getFirst();
            return new ChunkPos(SectionPos.blockToSectionCoord(position.getX()), SectionPos.blockToSectionCoord(position.getZ()));
        }
    }
}