package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.world.orevein.OreVeinSystem;
import com.farcr.nomansland.common.world.orevein.OreVeinSystem.OreVeinInstance;
import com.farcr.nomansland.common.world.orevein.OreVeinType;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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

        Registry<OreVeinType> registry = level.registryAccess().registryOrThrow(NMLRegistries.ORE_VEIN_KEY);
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        ChunkGenerator generator = level.getLevel().getChunkSource().getGenerator();
        WorldGenerationContext context = new WorldGenerationContext(generator, level);

        int chunkMinX = chunkPos.getMinBlockX();
        int chunkMinZ = chunkPos.getMinBlockZ();

        List<OreVeinInstance> veinsInChunk = new ArrayList<>();

        for (Holder.Reference<OreVeinType> typeHolder : registry.holders().toList()) {
            OreVeinType type = typeHolder.value();
            if (type.biomes().isEmpty()) continue;
            HolderSet<Biome> allowed = type.biomes().get();
            if (!allowed.contains(targetBiome)) continue;

            ResourceLocation typeId = typeHolder.key().location();
            int centerCellX = Math.floorDiv(chunkMinX, type.spacing());
            int centerCellZ = Math.floorDiv(chunkMinZ, type.spacing());

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Optional<OreVeinInstance> maybeInstance = createVeinForBiome(
                        type, typeId, centerCellX + dx, centerCellZ + dz, allowed,
                        context, randomState, generator, level, pieces
                    );
                    if (maybeInstance.isEmpty()) continue;

                    OreVeinInstance instance = maybeInstance.get();
                    if (Mth.length(chunkMinX - instance.x(), chunkMinZ - instance.z()) > instance.radius() + 24) continue;

                    veinsInChunk.add(instance);
                }
            }
        }

        if (veinsInChunk.isEmpty()) return;

        veinsInChunk.sort(Comparator
            .comparingInt((OreVeinInstance i) -> i.type().generationOrder())
            .thenComparingInt(i -> Math.abs(i.x()) + Math.abs(i.z())));

        OreVeinSystem.fill(veinsInChunk, level, chunk, randomState);
    }

    private static Optional<OreVeinInstance> createVeinForBiome(
        OreVeinType type, ResourceLocation typeId, int cellX, int cellZ,
        HolderSet<Biome> allowedBiomes,
        WorldGenerationContext context, RandomState randomState, ChunkGenerator generator,
        WorldGenLevel level,
        PiecesContainer pieces
    ) {
        RandomSource veinRandom = randomState.getOrCreateRandomFactory(typeId).at(cellX, 0, cellZ);

        if (veinRandom.nextFloat() > type.probability()) return Optional.empty();

        int minY = type.minHeight().sample(veinRandom, context);
        int maxY = type.maxHeight().sample(veinRandom, context);
        if (minY > maxY) return Optional.empty();

        int centerY = (minY + maxY) / 2;

        int minX = cellX * type.spacing();
        int minZ = cellZ * type.spacing();
        int maxX = minX + (type.spacing() - type.separation());
        int maxZ = minZ + (type.spacing() - type.separation());
        int centerX = veinRandom.nextInt(minX, maxX);
        int centerZ = veinRandom.nextInt(minZ, maxZ);

        int sampleY = centerY;
        if (type.sampleBiomeAtSurface()) {
            sampleY = generator.getBaseHeight(centerX, centerZ, Heightmap.Types.OCEAN_FLOOR, level, randomState);
        }

        Holder<Biome> sourceBiome = generator.getBiomeSource().getNoiseBiome(
            QuartPos.fromBlock(centerX), QuartPos.fromBlock(sampleY), QuartPos.fromBlock(centerZ),
            randomState.sampler());
        if (allowedBiomes.contains(sourceBiome)) return Optional.empty();

        if (!isInPieces(centerX, sampleY, centerZ, pieces)) return Optional.empty();

        int radius = type.radius().sample(veinRandom);
        if (radius <= 0) return Optional.empty();

        float veinRadius = type.veinRadius().sample(veinRandom);
        if (veinRadius <= 0) return Optional.empty();

        return Optional.of(new OreVeinInstance(type, centerX, centerZ, minY, maxY, radius, radius * radius, veinRadius));
    }

    private static boolean isInPieces(int x, int y, int z, PiecesContainer pieces) {
        for (StructurePiece piece : pieces.pieces()) {
            BoundingBox b = piece.getBoundingBox();
            if (x >= b.minX() && x <= b.maxX() && y >= b.minY() && y <= b.maxY() && z >= b.minZ() && z <= b.maxZ()) {
                return true;
            }
        }
        return false;
    }
}
