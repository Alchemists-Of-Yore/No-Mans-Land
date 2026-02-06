package com.farcr.nomansland.common.friend.dialogue;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class DialogueRegistry {
    public record DialoguePool(
        Optional<DialogueCondition> condition,
        Weight weight,
        String text
    ) implements WeightedEntry {
        public static final Codec<DialoguePool> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                DialogueCondition.CODEC.optionalFieldOf("condition").forGetter(DialoguePool::condition),
                Weight.CODEC.fieldOf("weight").forGetter(DialoguePool::getWeight),
                Codec.STRING.fieldOf("text").forGetter(DialoguePool::text)
            ).apply(instance, DialoguePool::new)
        );

        @Override
        public @NotNull Weight getWeight() {
            return weight;
        }
    }

    public interface DialogueCondition {
        default boolean validate(ResourceKey<Registry<DialoguePool>> registrykey) { return true; }
        public MapCodec<? extends DialogueCondition> codec();
        public static final Codec<DialogueCondition> CODEC = NMLRegistries.DIALOGUE_CONDITIONAL_TYPE
            .byNameCodec().dispatch(DialogueCondition::codec, Function.identity());
    }

    /*
    * Simple List Conditionals, for "hardcoded" conditions
     */
    public interface ListCondition extends DialogueCondition {
        public ArrayList<DialoguePool> getList();
        default void append(DialoguePool dialoguePool) {
            getList().add(dialoguePool);
        }
    }

    /*
    * Compiled Condition, for simple holder conditions
    * that need to be compiled to hashmaps at runtime.
     */
    public interface CompiledCondition<T> extends DialogueCondition {
        public HolderSet<T> getValue();
        public HashMap<T, ArrayList<DialoguePool>> getMap();
        default void consume(DialoguePool dialoguePool){
            getValue().forEach((holder) -> {
                ArrayList<DialoguePool> poolList = getMap().getOrDefault(holder.value(), new ArrayList<>());
                poolList.add(dialoguePool);
                getMap().put(holder.value(), poolList);
            });
        };
    }
}