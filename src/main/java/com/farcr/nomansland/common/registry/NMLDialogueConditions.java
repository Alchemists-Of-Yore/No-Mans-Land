package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.friend.condition.MoonlightGreetingConditions;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.friend.condition.MoonlightOfferingConditions;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NMLDialogueConditions {
    public static final DeferredRegister<MapCodec<? extends DialogueRegistry.DialogueCondition>> DIALOGUE_CONDITION_REGISTRY =
        DeferredRegister.create(NMLRegistries.DIALOGUE_CONDITIONAL_TYPE, NoMansLand.MODID);

    public static final DeferredHolder<MapCodec<? extends DialogueRegistry.DialogueCondition>, MapCodec<MoonlightOfferingConditions.ItemOfferingConditional>> OFFERING_ITEM_CONDITIONAL =
        DIALOGUE_CONDITION_REGISTRY.register("item_conditional", () -> MoonlightOfferingConditions.ItemOfferingConditional.CODEC);
    public static final DeferredHolder<MapCodec<? extends DialogueRegistry.DialogueCondition>, MapCodec<MoonlightOfferingConditions.EntityOfferingConditional>> OFFERING_ENTITY_CONDITIONAL =
        DIALOGUE_CONDITION_REGISTRY.register("entity_conditional", () -> MoonlightOfferingConditions.EntityOfferingConditional.CODEC);

    public static final DeferredHolder<MapCodec<? extends DialogueRegistry.DialogueCondition>, MapCodec<MoonlightGreetingConditions.FirstTimeGreetingConditional>> FIRST_TIME_GREETING_CONDITIONAL =
        DIALOGUE_CONDITION_REGISTRY.register("first_time", () -> MoonlightGreetingConditions.FirstTimeGreetingConditional.CODEC);
}
