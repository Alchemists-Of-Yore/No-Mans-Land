package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.extension.ChunkGeneratorExtension;
import com.farcr.nomansland.common.extension.ChunkGeneratorStructureStateExtension;
import com.farcr.nomansland.common.registry.worldgen.NMLStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
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
            ).optionalFieldOf("feature_placeholders", Map.of()).forGetter(s -> s.featurePlaceholders),
            HeightProvider.CODEC.optionalFieldOf("start_height").forGetter(s -> s.startHeight),
            Direction.CODEC.optionalFieldOf("template_facing", Direction.NORTH).forGetter(s -> s.templateFacing)
        ).apply(instance, MenhirStructure::new)
    );

    private final Holder<StructureTemplatePool> startPool;
    private final Heightmap.Types projectStartToHeightmap;
    private final Map<Block, ResourceKey<ConfiguredFeature<?, ?>>> featurePlaceholders;
    private final Optional<HeightProvider> startHeight;
    private final Direction templateFacing;

    public MenhirStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, Heightmap.Types projectStartToHeightmap, Map<Block, ResourceKey<ConfiguredFeature<?, ?>>> featurePlaceholders, Optional<HeightProvider> startHeight, Direction templateFacing) {
        super(settings);
        this.startPool = startPool;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.featurePlaceholders = featurePlaceholders;
        this.startHeight = startHeight;
        this.templateFacing = templateFacing;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        RandomSource random = context.random();
        StructureTemplateManager templates = context.structureTemplateManager();

        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();
        int surfaceY = context.chunkGenerator().getFirstFreeHeight(
            centerX, centerZ, projectStartToHeightmap,
            context.heightAccessor(), context.randomState()
        );
        if (startHeight.isPresent()) {
            surfaceY += startHeight.get().sample(random, new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        }
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

    private Rotation rotationTowardMeetingPoint(GenerationContext context, int centerX, int centerZ, RandomSource random) {
        ChunkGeneratorStructureState state = ((ChunkGeneratorExtension) context.chunkGenerator()).nomansland$structureState();
        if (state instanceof ChunkGeneratorStructureStateExtension extension) {
            ChunkPos meetingPoint = extension.meetingPointPosition();
            if (meetingPoint != null) {
                int dx = meetingPoint.getMiddleBlockX() - centerX;
                int dz = meetingPoint.getMiddleBlockZ() - centerZ;
                return rotationFromTo(templateFacing, directionTo(dx, dz));
            }
        }
        return Rotation.getRandom(random);
    }

    private static Direction directionTo(int dx, int dz) {
        if (Math.abs(dx) > Math.abs(dz)) {
            return dx > 0 ? Direction.EAST : Direction.WEST;
        }
        return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static Rotation rotationFromTo(Direction from, Direction to) {
        int delta = (to.get2DDataValue() - from.get2DDataValue() + 4) & 3;
        return switch (delta) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.MENHIR.get();
    }
}
