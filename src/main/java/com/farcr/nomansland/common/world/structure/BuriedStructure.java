package com.farcr.nomansland.common.world.structure;

import com.farcr.nomansland.common.registry.worldgen.NMLStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;

public class BuriedStructure extends EmbeddedStructure {
    public static final MapCodec<BuriedStructure> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    settingsCodec(instance),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    Codec.intRange(0, 20).optionalFieldOf("size", 7).forGetter(structure -> structure.maxDepth),
                    Codec.intRange(1, 128).optionalFieldOf("max_distance_from_center", 80).forGetter(structure -> structure.maxDistanceFromCenter),
                    HeightProvider.CODEC.optionalFieldOf("start_height", DEFAULT_START_HEIGHT).forGetter(structure -> structure.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    ExposureTreatment.CODEC.optionalFieldOf("exposure", ExposureTreatment.NONE).forGetter(structure -> structure.exposure),
                    Codec.BOOL.optionalFieldOf("preserve", false).forGetter(EmbeddedStructure::shouldPreserve)
            ).apply(instance, BuriedStructure::new)
    );

    private static final int BURIED_SEARCH = 32;

    public BuriedStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, int maxDepth, int maxDistanceFromCenter, HeightProvider startHeight, Optional<Heightmap.Types> projectStartToHeightmap, ExposureTreatment exposure, boolean preserve) {
        super(settings, startPool, maxDepth, maxDistanceFromCenter, startHeight, projectStartToHeightmap, exposure, preserve);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor level = context.heightAccessor();
        RandomState randomState = context.randomState();
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMiddleBlockX();
        int z = chunkPos.getMiddleBlockZ();

        int y = this.startHeight.sample(context.random(), new WorldGenerationContext(generator, level));
        if (this.projectStartToHeightmap.isPresent()) {
            y += generator.getFirstOccupiedHeight(x, z, this.projectStartToHeightmap.get(), level, randomState);
        }

        NoiseColumn column = generator.getBaseColumn(x, z, level, randomState);
        BlockPos blockPos = new BlockPos(x, nearestBuriedY(column, y, level), z);
        return JigsawPlacement.addPieces(
                context, this.startPool, Optional.empty(), this.maxDepth, blockPos,
                false, Optional.empty(), this.maxDistanceFromCenter,
                PoolAliasLookup.EMPTY, DimensionPadding.ZERO, LiquidSettings.IGNORE_WATERLOGGING
        );
    }

    private static int nearestBuriedY(NoiseColumn column, int target, LevelHeightAccessor level) {
        int min = level.getMinBuildHeight() + MIN_HEIGHT_ABOVE_WORLD_BOTTOM;
        int max = level.getMaxBuildHeight() - 2;
        int clampedTarget = Math.clamp(target, min, max);
        if (isBuried(column, clampedTarget)) return clampedTarget;
        for (int offset = 1; offset <= BURIED_SEARCH; offset++) {
            int down = clampedTarget - offset;
            if (down >= min && isBuried(column, down)) return down;
            int up = clampedTarget + offset;
            if (up <= max && isBuried(column, up)) return up;
        }
        return clampedTarget;
    }

    private static boolean isBuried(NoiseColumn column, int y) {
        return column.getBlock(y - 1).isSolid()
                && column.getBlock(y).isSolid()
                && column.getBlock(y + 1).isSolid()
                && column.getBlock(y + 2).isSolid();
    }

    @Override
    public StructureType<?> type() {
        return NMLStructureTypes.BURIED.get();
    }
}
