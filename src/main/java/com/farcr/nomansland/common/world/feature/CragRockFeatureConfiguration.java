package com.farcr.nomansland.common.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record CragRockFeatureConfiguration(
        IntProvider radius,
        IntProvider height,
        FloatProvider radiusStrength,
        FloatProvider noiseStrength,
        boolean generateSurfaceUnderwater,
        BlockStateProvider baseBlockProvider,
        BlockStateProvider soilBlockProvider,
        BlockStateProvider surfaceBlockProvider
) implements FeatureConfiguration {
    public static final Codec<CragRockFeatureConfiguration> CODEC = RecordCodecBuilder.create(
            record -> record.group(
                    IntProvider.codec(1, 8).fieldOf("radius").forGetter(CragRockFeatureConfiguration::radius),
                    IntProvider.POSITIVE_CODEC.fieldOf("height").forGetter(CragRockFeatureConfiguration::height),
                    FloatProvider.CODEC.fieldOf("radius_strength").orElse(ConstantFloat.of(1.0F)).forGetter(CragRockFeatureConfiguration::radiusStrength),
                    FloatProvider.CODEC.fieldOf("noise_strength" ).orElse(ConstantFloat.of(5.0F)).forGetter(CragRockFeatureConfiguration::noiseStrength),
                    Codec.BOOL.fieldOf("generate_surface_under_fluids").orElse(false).forGetter(CragRockFeatureConfiguration::generateSurfaceUnderwater),
                    BlockStateProvider.CODEC.fieldOf("base_block_provider").forGetter(CragRockFeatureConfiguration::baseBlockProvider),
                    BlockStateProvider.CODEC.fieldOf("soil_block_provider").forGetter(CragRockFeatureConfiguration::soilBlockProvider),
                    BlockStateProvider.CODEC.fieldOf("surface_block_provider").forGetter(CragRockFeatureConfiguration::surfaceBlockProvider)
            ).apply(record, CragRockFeatureConfiguration::new)
    );
}
