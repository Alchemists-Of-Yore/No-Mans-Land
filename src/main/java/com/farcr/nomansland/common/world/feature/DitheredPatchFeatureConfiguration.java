package com.farcr.nomansland.common.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record DitheredPatchFeatureConfiguration (
        IntProvider radius,
        IntProvider depth,
        FloatProvider radiusStrength,
        FloatProvider noiseStrength,
        FloatProvider ditherStrength,
        BlockStateProvider blockProvider,
        BlockPredicate target
) implements FeatureConfiguration {
    public static final Codec<DitheredPatchFeatureConfiguration> CODEC = RecordCodecBuilder.create(
            record -> record.group(
                    IntProvider.codec(1,  8).fieldOf("radius").orElse(ConstantInt.of(6)).forGetter(DitheredPatchFeatureConfiguration::radius),
                    IntProvider.POSITIVE_CODEC.fieldOf("depth").orElse(ConstantInt.of(1)).forGetter(DitheredPatchFeatureConfiguration::depth),
                    FloatProvider.CODEC.fieldOf("radius_strength").orElse(ConstantFloat.of(0.5F)).forGetter(DitheredPatchFeatureConfiguration::radiusStrength),
                    FloatProvider.CODEC.fieldOf("noise_strength" ).orElse(ConstantFloat.of(5.0F)).forGetter(DitheredPatchFeatureConfiguration::noiseStrength),
                    FloatProvider.CODEC.fieldOf("dither_strength").orElse(ConstantFloat.of(3.0F)).forGetter(DitheredPatchFeatureConfiguration::ditherStrength),
                    BlockStateProvider.CODEC.fieldOf("block_provider").forGetter(DitheredPatchFeatureConfiguration::blockProvider),
                    BlockPredicate.CODEC.fieldOf("target").forGetter(DitheredPatchFeatureConfiguration::target)
            ).apply(record, DitheredPatchFeatureConfiguration::new)
    );
}
