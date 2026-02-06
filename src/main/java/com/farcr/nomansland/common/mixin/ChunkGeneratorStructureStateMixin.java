package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.extension.ChunkGeneratorStructureStateExtension;
import com.farcr.nomansland.common.world.structure.MeetingPointStructurePlacement;
import com.google.common.base.Stopwatch;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    @Inject(method = "lambda$generatePositions$4", at = @At("TAIL"))
    private void generateMeetingPointPosition(Set<Holder<StructureSet>> possibleStructureSets, Holder<StructureSet> setHolder, CallbackInfo ci, @Local boolean hasAnyPlaceableStructures) {
        if (hasAnyPlaceableStructures && setHolder.value().placement() instanceof MeetingPointStructurePlacement meetingPointPlacement) {
            meetingPointPosition = generateMeetingPointPosition(setHolder.value(), meetingPointPlacement);
        }
    }

    private CompletableFuture<ChunkPos> generateMeetingPointPosition(StructureSet structureSet, MeetingPointStructurePlacement placement) {
        Stopwatch stopwatch = Stopwatch.createStarted(Util.TICKER);
        CompletableFuture<ChunkPos> task;
        HolderSet<Biome> preferredBiomes = placement.preferredBiomes;
        RandomSource random = RandomSource.create();
        random.setSeed(levelSeed);

        RandomSource biomeSearchGenerator = random.fork();
        task = CompletableFuture.supplyAsync(
                () -> {
                    Pair<BlockPos, Holder<Biome>> closestBiome = null;
                    int tries = 0;
                    while (closestBiome == null && tries < 10) {
                        double angle = random.nextDouble() * Math.PI * 2.0;
                        double distance = random.nextInt(1000, 5000);
                        int x = (int) Math.round(Math.cos(angle) * distance);
                        int z = (int) Math.round(Math.sin(angle) * distance);
                        closestBiome = findBiome(x, z, preferredBiomes, biomeSearchGenerator);
                        tries++;
                    }

                    if (closestBiome == null) {
                        double angle = random.nextDouble() * Math.PI * 2.0;
                        double distance = random.nextInt(1000, 5000);
                        int x = (int) Math.round(Math.cos(angle) * distance);
                        int z = (int) Math.round(Math.sin(angle) * distance);
                        return new ChunkPos(new BlockPos(x, 0, z));
                    } else {
                        BlockPos position = closestBiome.getFirst();
                        return new ChunkPos(SectionPos.blockToSectionCoord(position.getX()), SectionPos.blockToSectionCoord(position.getZ()));
                    }
                }, Util.backgroundExecutor()
        );

        return task.thenApply(meetingPointPosition -> {
            double elapsedSeconds = stopwatch.stop().elapsed(TimeUnit.MILLISECONDS) / 1000.0;
            NoMansLand.LOGGER.debug("Calculation for {} took {}s", structureSet, elapsedSeconds);
            NoMansLand.LOGGER.debug("Meeting Point Position is: {} {}", SectionPos.sectionToBlockCoord(meetingPointPosition.x), SectionPos.sectionToBlockCoord(meetingPointPosition.z));
            return meetingPointPosition;
        });
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
        ensureStructuresGenerated();
        return meetingPointPosition == null ? null : meetingPointPosition.join();
    }
}