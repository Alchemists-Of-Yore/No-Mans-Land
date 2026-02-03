package com.farcr.nomansland.common.friend.condition;

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

        public static ArrayList<DialogueRegistry.DialoguePool> FIRST_TIME_ARRAY = new ArrayList<>();

        @Override
        public boolean validate(ResourceKey<Registry<DialogueRegistry.DialoguePool>> resourceKey) {
            return resourceKey.equals(NMLRegistries.GREETING_DIALOGUE_KEY);
        }

        @Override
        public ArrayList<DialogueRegistry.DialoguePool> getList() {
            return FIRST_TIME_ARRAY;
        }
    }

}
