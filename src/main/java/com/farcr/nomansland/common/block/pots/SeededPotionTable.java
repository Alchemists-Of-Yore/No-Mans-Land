package com.farcr.nomansland.common.block.pots;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record SeededPotionTable(ResourceLocation potionTable, long seed) {
    public static final Codec<SeededPotionTable> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("potion_table").forGetter(SeededPotionTable::potionTable),
                    Codec.LONG.optionalFieldOf("seed", 0L).forGetter(SeededPotionTable::seed)
            ).apply(instance, SeededPotionTable::new)
    );
}
