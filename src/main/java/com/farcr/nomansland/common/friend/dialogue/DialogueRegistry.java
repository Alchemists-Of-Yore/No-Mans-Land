package com.farcr.nomansland.common.friend.dialogue;

import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;

import java.util.*;
import java.util.function.Function;

public class DialogueRegistry {
    public record DialoguePool(
        Optional<DialogueCondition> condition,
        float weight,
        String text
    ) {
        public static final Codec<DialoguePool> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                DialogueCondition.CODEC.optionalFieldOf("condition").forGetter(DialoguePool::condition),
                Codec.FLOAT.fieldOf("weight").forGetter(DialoguePool::weight),
                Codec.STRING.fieldOf("text").forGetter(DialoguePool::text)
            ).apply(instance, DialoguePool::new)
        );
    }

    public interface DialogueCondition {
        public MapCodec<? extends DialogueCondition> codec();
        public static final Codec<DialogueCondition> CODEC = NMLRegistries.DIALOGUE_CONDITIONAL_TYPE
            .byNameCodec().dispatch(DialogueCondition::codec, Function.identity());
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