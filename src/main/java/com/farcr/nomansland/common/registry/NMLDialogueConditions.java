package com.farcr.nomansland.common.registry;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.dialogue.condition.MoonlightOfferingConditions;
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
}
