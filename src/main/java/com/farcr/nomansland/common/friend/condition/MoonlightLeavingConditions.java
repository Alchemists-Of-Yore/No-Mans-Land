package com.farcr.nomansland.common.friend.condition;

import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;

public class MoonlightLeavingConditions {
    public record OnDeathConditional() implements DialogueRegistry.ListCondition {
        public static final MapCodec<OnDeathConditional> CODEC =
            MapCodec.unit(OnDeathConditional::new);

        @Override
        public MapCodec<? extends DialogueRegistry.DialogueCondition> codec() {
            return CODEC;
        }

        public static ArrayList<DialogueRegistry.DialoguePool> ON_DEATH_ARRAY = new ArrayList<>();

        @Override
        public boolean validate(ResourceKey<Registry<DialogueRegistry.DialoguePool>> resourceKey) {
            return resourceKey.equals(NMLRegistries.LEAVING_DIALOGUE_KEY);
        }

        @Override
        public ArrayList<DialogueRegistry.DialoguePool> getList() {
            return ON_DEATH_ARRAY;
        }
    }
}
