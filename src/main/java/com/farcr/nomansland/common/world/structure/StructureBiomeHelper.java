package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.world.orevein.OreVeinSystem;
import com.farcr.nomansland.common.world.orevein.OreVeinSystem.OreVeinInstance;
import com.farcr.nomansland.common.world.orevein.OreVeinType;
import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class StructureBiomeHelper {
    private StructureBiomeHelper() {}

    public static void applyBiome(
        Holder<Biome> biome,
        WorldGenLevel level,
        ChunkAccess chunk,
        ChunkGenerator generator,
        ChunkPos chunkPos,
        PiecesContainer pieces
    ) {
        if (pieces.pieces().isEmpty()) return;

        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMaxX = chunkPos.getMaxBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();
        int chunkMaxZ = chunkPos.getMaxBlockZ();

        List<BoundingBox> pieceBoxes = pieces.pieces().stream()
            .map(StructurePiece::getBoundingBox)
            .filter(b -> b.maxX() >= chunkMinX && b.minX() <= chunkMaxX
                && b.maxZ() >= chunkMinZ && b.minZ() <= chunkMaxZ)
            .toList();
        if (pieceBoxes.isEmpty()) return;

        rewriteBiomeCells(biome, level, chunk, chunkPos, pieceBoxes);
        placeBiomeFeatures(biome, level, generator, chunkMinX, chunkMinZ);
        placeBiomeVeins(biome, level, chunk, chunkPos, pieces);
    }

    private static void rewriteBiomeCells(
        Holder<Biome> biome,
        WorldGenLevel level,
        ChunkAccess chunk,
        ChunkPos chunkPos,
        List<BoundingBox> pieceBoxes
    ) {
        int structMinY = pieceBoxes.stream().mapToInt(BoundingBox::minY).min().getAsInt();
        int structMaxY = pieceBoxes.stream().mapToInt(BoundingBox::maxY).max().getAsInt();

        int minSection = Math.max(0, chunk.getSectionIndex(structMinY));
        int maxSection = Math.min(chunk.getSectionsCount() - 1, chunk.getSectionIndex(structMaxY));
        int baseQuartY = QuartPos.fromBlock(level.getMinBuildHeight());

        for (int si = minSection; si <= maxSection; si++) {
            LevelChunkSection section = chunk.getSection(si);
            var newBiomes = section.biomes.recreate();

            for (int lx = 0; lx < 4; lx++) {
                for (int lz = 0; lz < 4; lz++) {
                    for (int ly = 0; ly < 4; ly++) {
                        Holder<Biome> existing = section.biomes.get(lx, ly, lz);
                        Holder<Biome> next = existing;

                        int gqx = chunkPos.x * 4 + lx;
                        int gqy = baseQuartY + si * 4 + ly;
                        int gqz = chunkPos.z * 4 + lz;
                        int cellMinX = QuartPos.toBlock(gqx);
                        int cellMinY = QuartPos.toBlock(gqy);
                        int cellMinZ = QuartPos.toBlock(gqz);
                        int cellMaxX = cellMinX + 3;
                        int cellMaxY = cellMinY + 3;
                        int cellMaxZ = cellMinZ + 3;

                        for (BoundingBox pb : pieceBoxes) {
                            if (pb.maxX() >= cellMinX && pb.minX() <= cellMaxX
                                    && pb.maxY() >= cellMinY && pb.minY() <= cellMaxY
                                    && pb.maxZ() >= cellMinZ && pb.minZ() <= cellMaxZ) {
                                next = biome;
                                break;
                            }
                        }

                        newBiomes.set(lx, ly, lz, next);
                    }
                }
            }

            section.biomes = newBiomes;
        }
    }

    private static void placeBiomeFeatures(
        Holder<Biome> biome,
        WorldGenLevel level,
        ChunkGenerator generator,
        int chunkMinX,
        int chunkMinZ
    ) {
        BlockPos origin = new BlockPos(chunkMinX, level.getMinBuildHeight(), chunkMinZ);
        WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
        long decorationSeed = random.setDecorationSeed(level.getSeed(), chunkMinX, chunkMinZ);
        List<HolderSet<PlacedFeature>> stepFeatures = biome.value().getGenerationSettings().features();

        for (int step = 0; step < stepFeatures.size(); step++) {
            int idx = 0;
            for (Holder<PlacedFeature> placed : stepFeatures.get(step)) {
                random.setFeatureSeed(decorationSeed, idx, step);
                placed.value().placeWithBiomeCheck(level, generator, random, origin);
                idx++;
            }
        }
    }

    private static void placeBiomeVeins(
        Holder<Biome> targetBiome,
        WorldGenLevel level,
        ChunkAccess chunk,
        ChunkPos chunkPos,
        PiecesContainer pieces
    ) {
        if (level.getLevel().dimension() != Level.OVERWORLD) return;
        if (pieces.pieces().isEmpty()) return;

        Optional<BoundingBox> maybeOverall = BoundingBox.encapsulatingBoxes(
            pieces.pieces().stream().map(StructurePiece::getBoundingBox).toList()
        );
        if (maybeOverall.isEmpty()) return;
        BoundingBox overall = maybeOverall.get();

        Registry<OreVeinType> registry = level.registryAccess().registryOrThrow(NMLRegistries.ORE_VEIN_KEY);
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        ChunkGenerator generator = level.getLevel().getChunkSource().getGenerator();
        WorldGenerationContext context = new WorldGenerationContext(generator, level);

        List<OreVeinInstance> veins = new ArrayList<>();

        for (Holder.Reference<OreVeinType> typeHolder : registry.holders().toList()) {
            OreVeinType type = typeHolder.value();
            if (type.biomes().isEmpty()) continue;
            HolderSet<Biome> allowed = type.biomes().get();
            if (!allowed.contains(targetBiome)) continue;

            int spacing = type.spacing();
            int window = spacing - type.separation();
            int minCellX = Math.floorDiv(overall.minX() - window + 1, spacing);
            int maxCellX = Math.floorDiv(overall.maxX(), spacing);
            int minCellZ = Math.floorDiv(overall.minZ() - window + 1, spacing);
            int maxCellZ = Math.floorDiv(overall.maxZ(), spacing);

            for (int cellX = minCellX; cellX <= maxCellX; cellX++) {
                for (int cellZ = minCellZ; cellZ <= maxCellZ; cellZ++) {
                    int cellMinX = cellX * spacing;
                    int cellMinZ = cellZ * spacing;
                    int rangeMinX = Math.max(cellMinX, overall.minX());
                    int rangeMaxX = Math.min(cellMinX + window, overall.maxX() + 1);
                    int rangeMinZ = Math.max(cellMinZ, overall.minZ());
                    int rangeMaxZ = Math.min(cellMinZ + window, overall.maxZ() + 1);
                    if (rangeMinX >= rangeMaxX || rangeMinZ >= rangeMaxZ) continue;

                    RandomSource veinRandom = randomState
                        .getOrCreateRandomFactory(typeHolder.key().location())
                        .at(cellX, 0, cellZ);

                    if (veinRandom.nextFloat() > type.probability()) continue;

                    int minY = type.minHeight().sample(veinRandom, context);
                    int maxY = type.maxHeight().sample(veinRandom, context);
                    if (minY > maxY) continue;

                    int centerX = veinRandom.nextInt(rangeMinX, rangeMaxX);
                    int centerZ = veinRandom.nextInt(rangeMinZ, rangeMaxZ);

                    int sampleY = type.sampleBiomeAtSurface()
                        ? generator.getBaseHeight(centerX, centerZ, Heightmap.Types.OCEAN_FLOOR, level, randomState)
                        : (minY + maxY) / 2;
                    Holder<Biome> sourceBiome = generator.getBiomeSource().getNoiseBiome(
                        QuartPos.fromBlock(centerX), QuartPos.fromBlock(sampleY), QuartPos.fromBlock(centerZ),
                        randomState.sampler());
                    if (allowed.contains(sourceBiome)) continue;

                    int radius = type.radius().sample(veinRandom);
                    if (radius <= 0) continue;
                    float veinRadius = type.veinRadius().sample(veinRandom);
                    if (veinRadius <= 0) continue;

                    veins.add(new OreVeinInstance(type, centerX, centerZ, minY, maxY, radius, radius * radius, veinRadius));
                }
            }
        }

        if (veins.isEmpty()) return;

        veins.sort(Comparator
            .comparingInt((OreVeinInstance i) -> i.type().generationOrder())
            .thenComparingInt(i -> Math.abs(i.x()) + Math.abs(i.z())));

        OreVeinSystem.fill(veins, level, chunk, randomState);
    }
}
