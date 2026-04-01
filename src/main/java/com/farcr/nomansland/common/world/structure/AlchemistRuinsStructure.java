package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.farcr.nomansland.common.registry.worldgen.NMLStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;

public class AlchemistRuinsStructure extends Structure {
    public static final MapCodec<AlchemistRuinsStructure> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
            Codec.intRange(0, 20).fieldOf("size").forGetter(s -> s.maxDepth),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
            Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistanceFromCenter),
            Codec.BOOL.optionalFieldOf("use_expansion_hack", false).forGetter(s -> s.useExpansionHack),
            Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap),
            DimensionPadding.CODEC.optionalFieldOf("dimension_padding", DimensionPadding.ZERO).forGetter(s -> s.dimensionPadding),
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings", LiquidSettings.APPLY_WATERLOGGING).forGetter(s -> s.liquidSettings)
        ).apply(instance, AlchemistRuinsStructure::new)
    );

    private final Holder<StructureTemplatePool> startPool;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final int maxDistanceFromCenter;
    private final boolean useExpansionHack;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;

    public AlchemistRuinsStructure(
        StructureSettings settings,
        Holder<StructureTemplatePool> startPool,
        int maxDepth,
        HeightProvider startHeight,
        int maxDistanceFromCenter,
        boolean useExpansionHack,
        Optional<Heightmap.Types> projectStartToHeightmap,
        DimensionPadding dimensionPadding,
        LiquidSettings liquidSettings
    ) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.useExpansionHack = useExpansionHack;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int y = startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        BlockPos blockPos = new BlockPos(chunkPos.getMiddleBlockX(), y, chunkPos.getMiddleBlockZ());
        return JigsawPlacement.addPieces(
            context, startPool, Optional.empty(), maxDepth, blockPos,
            useExpansionHack, projectStartToHeightmap, maxDistanceFromCenter,
            PoolAliasLookup.EMPTY, dimensionPadding, liquidSettings
        );
    }

    @Override
    public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, PiecesContainer pieces) {
        super.afterPlace(level, structureManager, generator, random, box, chunkPos, pieces);

        if (pieces.pieces().isEmpty()) return;

        Holder<Biome> alchemistBiome = level.registryAccess()
            .registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(NMLBiomes.ALCHEMIST_RUINS);

        int structMinX = pieces.pieces().stream().mapToInt(p -> p.getBoundingBox().minX()).min().getAsInt();
        int structMaxX = pieces.pieces().stream().mapToInt(p -> p.getBoundingBox().maxX()).max().getAsInt();
        int structMinY = pieces.pieces().stream().mapToInt(p -> p.getBoundingBox().minY()).min().getAsInt();
        int structMaxY = pieces.pieces().stream().mapToInt(p -> p.getBoundingBox().maxY()).max().getAsInt();
        int structMinZ = pieces.pieces().stream().mapToInt(p -> p.getBoundingBox().minZ()).min().getAsInt();
        int structMaxZ = pieces.pieces().stream().mapToInt(p -> p.getBoundingBox().maxZ()).max().getAsInt();

        int minX = Math.max(structMinX, chunkPos.getMinBlockX());
        int maxX = Math.min(structMaxX, chunkPos.getMaxBlockX());
        int minZ = Math.max(structMinZ, chunkPos.getMinBlockZ());
        int maxZ = Math.min(structMaxZ, chunkPos.getMaxBlockZ());

        // Structure bounds in quart coords (for XZ: clamped to this chunk; for Y: full struct range)
        int minQX = QuartPos.fromBlock(minX);
        int maxQX = QuartPos.fromBlock(maxX);
        int minQY = QuartPos.fromBlock(structMinY);
        int maxQY = QuartPos.fromBlock(structMaxY);
        int minQZ = QuartPos.fromBlock(minZ);
        int maxQZ = QuartPos.fromBlock(maxZ);

        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        int minSection = Math.max(0, chunk.getSectionIndex(structMinY));
        int maxSection = Math.min(chunk.getSectionsCount() - 1, chunk.getSectionIndex(structMaxY));
        int baseQuartY = QuartPos.fromBlock(level.getMinBuildHeight());

        for (int si = minSection; si <= maxSection; si++) {
            LevelChunkSection section = chunk.getSection(si);
            var newBiomes = section.biomes.recreate();

            for (int lx = 0; lx < 4; lx++) {
                for (int lz = 0; lz < 4; lz++) {
                    for (int ly = 0; ly < 4; ly++) {
                        int gqx = chunkPos.x * 4 + lx;
                        int gqy = baseQuartY + si * 4 + ly;
                        int gqz = chunkPos.z * 4 + lz;

                        Holder<Biome> biome = (gqx >= minQX && gqx <= maxQX
                            && gqy >= minQY && gqy <= maxQY
                            && gqz >= minQZ && gqz <= maxQZ)
                            ? alchemistBiome
                            : section.biomes.get(lx, ly, lz);

                        newBiomes.set(lx, ly, lz, biome);
                    }
                }
            }

            section.biomes = newBiomes;
        }
    }

    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.ALCHEMIST_RUINS.get();
    }
}
