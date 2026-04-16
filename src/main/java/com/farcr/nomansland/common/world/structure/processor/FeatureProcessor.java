package com.farcr.nomansland.common.world.structure.processor;

import com.farcr.nomansland.common.registry.worldgen.NMLStructureProcessorTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeatureProcessor extends StructureProcessor {
    public static final MapCodec<FeatureProcessor> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Codec.unboundedMap(
                BuiltInRegistries.BLOCK.byNameCodec(),
                ResourceKey.codec(Registries.CONFIGURED_FEATURE)
            ).fieldOf("placeholders").forGetter(p -> p.placeholders)
        ).apply(instance, FeatureProcessor::new)
    );

    private final Map<Block, ResourceKey<ConfiguredFeature<?, ?>>> placeholders;

    public FeatureProcessor(Map<Block, ResourceKey<ConfiguredFeature<?, ?>>> placeholders) {
        this.placeholders = placeholders;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return NMLStructureProcessorTypes.FEATURE.get();
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
        ServerLevelAccessor level,
        BlockPos offset,
        BlockPos pos,
        List<StructureTemplate.StructureBlockInfo> originalBlockInfos,
        List<StructureTemplate.StructureBlockInfo> processedBlockInfos,
        StructurePlaceSettings settings
    ) {
        List<StructureTemplate.StructureBlockInfo> result = new ArrayList<>(List.copyOf(processedBlockInfos));

        if (!(level instanceof WorldGenLevel worldGenLevel)) return result;

        for (StructureTemplate.StructureBlockInfo info : processedBlockInfos) {
            ResourceKey<ConfiguredFeature<?, ?>> featureKey = placeholders.get(info.state().getBlock());
            if (featureKey == null) continue;

            BlockState replacement = Blocks.AIR.defaultBlockState();
            for (Direction direction : Direction.values()) {
                if (direction != Direction.DOWN && level.getBlockState(info.pos().relative(direction)).is(Blocks.WATER)) {
                    replacement = Blocks.WATER.defaultBlockState();
                    break;
                }
            }

            result.remove(info);
            level.setBlock(info.pos(), replacement, 2);

            worldGenLevel.registryAccess()
                .registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getHolder(featureKey)
                .ifPresent(holder -> holder.value().place(
                    worldGenLevel,
                    worldGenLevel.getLevel().getChunkSource().getGenerator(),
                    settings.getRandom(info.pos()),
                    info.pos()
                ));
        }

        return result;
    }
}
