package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.extension.ChunkGeneratorExtension;
import com.farcr.nomansland.common.extension.ChunkGeneratorStructureStateExtension;
import com.farcr.nomansland.common.registry.worldgen.NMLStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Map;
import java.util.Optional;

public class MenhirStructure extends Structure {
    public static final MapCodec<MenhirStructure> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
            Heightmap.Types.CODEC.fieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap),
            Codec.unboundedMap(
                BuiltInRegistries.BLOCK.byNameCodec(),
                ResourceKey.codec(Registries.CONFIGURED_FEATURE)
            ).optionalFieldOf("feature_placeholders", Map.of()).forGetter(s -> s.featurePlaceholders)
        ).apply(instance, MenhirStructure::new)
    );

    private final Holder<StructureTemplatePool> startPool;
    private final Heightmap.Types projectStartToHeightmap;
    private final Map<Block, ResourceKey<ConfiguredFeature<?, ?>>> featurePlaceholders;

    public MenhirStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, Heightmap.Types projectStartToHeightmap, Map<Block, ResourceKey<ConfiguredFeature<?, ?>>> featurePlaceholders) {
        super(settings);
        this.startPool = startPool;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.featurePlaceholders = featurePlaceholders;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        RandomSource random = context.random();
        StructureTemplateManager templates = context.structureTemplateManager();

        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
            centerX, centerZ, projectStartToHeightmap,
            context.heightAccessor(), context.randomState()
        );
        BlockPos blockPos = new BlockPos(centerX, surfaceY, centerZ);

        Rotation rotation = rotationTowardMeetingPoint(context, centerX, centerZ, random);

        StructurePoolElement element = startPool.value().getRandomTemplate(random);
        BoundingBox box = element.getBoundingBox(templates, blockPos, rotation);
        Rotation finalRotation = rotation;

        return Optional.of(new GenerationStub(blockPos, builder ->
            builder.addPiece(new PoolElementStructurePiece(
                templates, element, blockPos, 0, finalRotation, box,
                LiquidSettings.IGNORE_WATERLOGGING
            ))
        ));
    }

    @Override
    public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox box, ChunkPos chunkPos, PiecesContainer pieces) {
        super.afterPlace(level, structureManager, generator, random, box, chunkPos, pieces);
        StructureFeatureHelper.replacePlaceholders(featurePlaceholders, level, generator, random, chunkPos, pieces);
    }

    private static Rotation rotationTowardMeetingPoint(GenerationContext context, int centerX, int centerZ, RandomSource random) {
        ChunkGeneratorStructureState state = ((ChunkGeneratorExtension) context.chunkGenerator()).nomansland$structureState();
        if (state instanceof ChunkGeneratorStructureStateExtension extension) {
            ChunkPos meetingPoint = extension.meetingPointPosition();
            if (meetingPoint != null) {
                int dx = meetingPoint.getMiddleBlockX() - centerX;
                int dz = meetingPoint.getMiddleBlockZ() - centerZ;
                return facingRotation(dx, dz);
            }
        }
        return Rotation.getRandom(random);
    }

    private static Rotation facingRotation(int dx, int dz) {
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
        } else {
            return dz >= 0 ? Rotation.NONE : Rotation.CLOCKWISE_180;
        }
    }

    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.MENHIR.get();
    }
}
