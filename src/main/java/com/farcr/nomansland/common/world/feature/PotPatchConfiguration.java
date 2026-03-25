package com.farcr.nomansland.common.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.List;
import java.util.Optional;

public record PotPatchConfiguration(
        List<WeightedVariant> variants,
        int tries,
        int xzSpread,
        int ySpread,
        Optional<ResourceLocation> lootTable,
        float potionChance,
        Optional<ResourceLocation> potionTable
) implements FeatureConfiguration {

    public record WeightedVariant(ResourceLocation variant, int weight) {
        public static final Codec<WeightedVariant> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("variant").forGetter(WeightedVariant::variant),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(WeightedVariant::weight)
        ).apply(instance, WeightedVariant::new));
    }

    public static final Codec<PotPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WeightedVariant.CODEC.listOf().fieldOf("variants").forGetter(PotPatchConfiguration::variants),
            Codec.INT.optionalFieldOf("tries", 16).forGetter(PotPatchConfiguration::tries),
            Codec.INT.optionalFieldOf("xz_spread", 4).forGetter(PotPatchConfiguration::xzSpread),
            Codec.INT.optionalFieldOf("y_spread", 3).forGetter(PotPatchConfiguration::ySpread),
            ResourceLocation.CODEC.optionalFieldOf("loot_table").forGetter(PotPatchConfiguration::lootTable),
            Codec.FLOAT.optionalFieldOf("potion_chance", 0f).forGetter(PotPatchConfiguration::potionChance),
            ResourceLocation.CODEC.optionalFieldOf("potion_table").forGetter(PotPatchConfiguration::potionTable)
    ).apply(instance, PotPatchConfiguration::new));
}
