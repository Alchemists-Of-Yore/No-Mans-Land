package com.farcr.nomansland.common.world.feature;

import com.farcr.nomansland.common.world.feature.decorator.BoulderDecorator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.List;

public record BoulderFeatureConfiguration(IntProvider numCubes, IntProvider numErodedBlocks, IntProvider cubeHeight, float cubeHeightDecrease, BlockStateProvider blockProvider, List<BoulderDecorator> decorators) implements FeatureConfiguration {
    public static final Codec<BoulderFeatureConfiguration> CODEC = RecordCodecBuilder.create(
            record -> record.group(
                    IntProvider.codec(1, 16).fieldOf("num_cubes").forGetter(BoulderFeatureConfiguration::numCubes),
                    IntProvider.codec(0, 32).fieldOf("num_eroded_blocks").forGetter(BoulderFeatureConfiguration::numErodedBlocks),
                    IntProvider.codec(1, 32).fieldOf("cube_height").forGetter(BoulderFeatureConfiguration::cubeHeight),
                    Codec.FLOAT.fieldOf("cube_height_decrease").forGetter(BoulderFeatureConfiguration::cubeHeightDecrease),
                    BlockStateProvider.CODEC.fieldOf("block_provider").forGetter(BoulderFeatureConfiguration::blockProvider),
                    BoulderDecorator.CODEC.listOf().fieldOf("decorators").orElse(List.of()).forGetter(BoulderFeatureConfiguration::decorators)
            ).apply(record, BoulderFeatureConfiguration::new)
    );
}
