package com.farcr.nomansland.common.friend.condition;

import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;

public class MoonlightGreetingConditions {
    public record FirstTimeGreetingConditional() implements DialogueRegistry.ListCondition {
        public static final MapCodec<FirstTimeGreetingConditional> CODEC =
            MapCodec.unit(FirstTimeGreetingConditional::new);

        @Override
        public MapCodec<? extends DialogueRegistry.DialogueCondition> codec() {
            return CODEC;
        }

        public static ArrayList<DialoguePool> FIRST_TIME_ARRAY = new ArrayList<>();

        @Override
        public boolean validate(ResourceKey<Registry<DialoguePool>> resourceKey) {
            return resourceKey.equals(NMLRegistries.GREETING_DIALOGUE_KEY);
        }

        @Override
        public ArrayList<DialoguePool> getList() {
            return FIRST_TIME_ARRAY;
        }
    }

    public record AdditionToCommuneConditional() implements DialogueRegistry.ListCondition {
        public static final MapCodec<AdditionToCommuneConditional> CODEC =
            MapCodec.unit(AdditionToCommuneConditional::new);

        @Override
        public MapCodec<? extends DialogueRegistry.DialogueCondition> codec() {
            return CODEC;
        }

        public static ArrayList<DialoguePool> ADDITION_TO_COMMUNE_ARRAY = new ArrayList<>();

        @Override
        public boolean validate(ResourceKey<Registry<DialoguePool>> resourceKey) {
            return resourceKey.equals(NMLRegistries.GREETING_DIALOGUE_KEY);
        }

        @Override
        public ArrayList<DialoguePool> getList() {
            return ADDITION_TO_COMMUNE_ARRAY;
        }
    }

    public record DreamGreetingConditional() implements DialogueRegistry.ListCondition {
        public static final MapCodec<DreamGreetingConditional> CODEC =
            MapCodec.unit(DreamGreetingConditional::new);

        @Override
        public MapCodec<? extends DialogueRegistry.DialogueCondition> codec() {
            return CODEC;
        }

        public static ArrayList<DialoguePool> DREAM_ARRAY = new ArrayList<>();

        @Override
        public boolean validate(ResourceKey<Registry<DialoguePool>> resourceKey) {
            return resourceKey.equals(NMLRegistries.GREETING_DIALOGUE_KEY);
        }

        @Override
        public ArrayList<DialoguePool> getList() {
            return DREAM_ARRAY;
        }
    }

}
