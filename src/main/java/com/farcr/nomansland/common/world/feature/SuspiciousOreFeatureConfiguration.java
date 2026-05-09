package com.farcr.nomansland.common.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

import java.util.List;
import java.util.Optional;

public record SuspiciousOreFeatureConfiguration(
        List<OreConfiguration.TargetBlockState> targetStates,
        int size,
        float discardChanceOnAirExposure,
        BlockState suspiciousState,
        float suspiciousChance,
        Optional<ResourceLocation> lootTable
) implements FeatureConfiguration {
    public static final Codec<SuspiciousOreFeatureConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter(SuspiciousOreFeatureConfiguration::targetStates),
            Codec.intRange(0, 64).fieldOf("size").forGetter(SuspiciousOreFeatureConfiguration::size),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("discard_chance_on_air_exposure", 0.0F).forGetter(SuspiciousOreFeatureConfiguration::discardChanceOnAirExposure),
            BlockState.CODEC.fieldOf("suspicious_state").forGetter(SuspiciousOreFeatureConfiguration::suspiciousState),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("suspicious_chance", 0.1F).forGetter(SuspiciousOreFeatureConfiguration::suspiciousChance),
            ResourceLocation.CODEC.optionalFieldOf("loot_table").forGetter(SuspiciousOreFeatureConfiguration::lootTable)
    ).apply(instance, SuspiciousOreFeatureConfiguration::new));
}
