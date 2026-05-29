package com.farcr.nomansland.common.block.pots;

import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    public static PotionContents resolve(Level level, @Nullable ResourceLocation tableId, long seed) {
        if (tableId == null) return null;
        Registry<PotionTable> registry = level.registryAccess().registryOrThrow(NMLRegistries.POTION_TABLE_KEY);
        PotionTable table = registry.getOptional(ResourceKey.create(NMLRegistries.POTION_TABLE_KEY, tableId)).orElse(null);
        if (table == null) return null;
        RandomSource random = seed != 0L ? RandomSource.create(seed) : level.getRandom();
        return table.select(random);
    }
}
