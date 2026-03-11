package com.farcr.nomansland.common.friend.dialogue;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record DialoguePool(
    Optional<DialogueRegistry.DialogueCondition> condition,
    Weight weight,
    String text
) implements WeightedEntry {
    public static final Codec<DialoguePool> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            DialogueRegistry.DialogueCondition.CODEC.optionalFieldOf("condition").forGetter(DialoguePool::condition),
            Weight.CODEC.fieldOf("weight").forGetter(DialoguePool::getWeight),
            Codec.STRING.fieldOf("text").forGetter(DialoguePool::text)
        ).apply(instance, DialoguePool::new)
    );

    @Override
    public @NotNull Weight getWeight() {
        return weight;
    }
}