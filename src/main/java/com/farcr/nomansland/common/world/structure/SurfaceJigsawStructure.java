package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.registry.worldgen.NMLStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;

public class SurfaceJigsawStructure extends Structure {
    public static final MapCodec<SurfaceJigsawStructure> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
            Codec.intRange(0, 7).fieldOf("size").forGetter(s -> s.maxDepth),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight),
            Heightmap.Types.CODEC.fieldOf("project_start_to_heightmap").forGetter(s -> s.projectStartToHeightmap),
            Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(s -> s.maxDistanceFromCenter),
            Codec.INT.fieldOf("min_y").forGetter(s -> s.minY)
        ).apply(instance, SurfaceJigsawStructure::new)
    );

    private final Holder<StructureTemplatePool> startPool;
    private final int maxDepth;
    private final HeightProvider startHeight;
    private final Heightmap.Types projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final int minY;

    public SurfaceJigsawStructure(
        StructureSettings settings,
        Holder<StructureTemplatePool> startPool,
        int maxDepth,
        HeightProvider startHeight,
        Heightmap.Types projectStartToHeightmap,
        int maxDistanceFromCenter,
        int minY
    ) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.minY = minY;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
            chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ(),
            projectStartToHeightmap, context.heightAccessor(), context.randomState()
        );

        if (surfaceY < minY)
            return Optional.empty();

        BlockPos blockPos = new BlockPos(chunkPos.getMiddleBlockX(), surfaceY, chunkPos.getMiddleBlockZ());
        return JigsawPlacement.addPieces(
            context, startPool, Optional.empty(), maxDepth, blockPos, false,
            Optional.of(projectStartToHeightmap), maxDistanceFromCenter,
            PoolAliasLookup.EMPTY, DimensionPadding.ZERO, LiquidSettings.APPLY_WATERLOGGING
        );
    }

    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.SURFACE_JIGSAW.get();
    }
}
