package com.farcr.nomansland.common.block.pots;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.List;

public record PotionTable(List<WeightedPotion> potions) {

    public record WeightedPotion(PotionContents potion, int weight) {
        public static final Codec<WeightedPotion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PotionContents.CODEC.fieldOf("potion").forGetter(WeightedPotion::potion),
                Codec.INT.optionalFieldOf("weight", 1).forGetter(WeightedPotion::weight)
        ).apply(instance, WeightedPotion::new));
    }

    public static final Codec<PotionTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            WeightedPotion.CODEC.listOf().fieldOf("potions").forGetter(PotionTable::potions)
    ).apply(instance, PotionTable::new));

    public PotionContents select(RandomSource random) {
        int totalWeight = potions.stream().mapToInt(WeightedPotion::weight).sum();
        if (totalWeight <= 0) return PotionContents.EMPTY;
        int roll = random.nextInt(totalWeight);
        for (WeightedPotion entry : potions) {
            roll -= entry.weight();
            if (roll < 0) return entry.potion();
        }
        return potions.getLast().potion();
    }
}
