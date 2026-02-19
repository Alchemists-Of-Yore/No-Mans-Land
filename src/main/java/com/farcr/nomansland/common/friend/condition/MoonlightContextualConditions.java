package com.farcr.nomansland.common.friend.condition;

import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;

public class MoonlightContextualConditions {
    public record EffectContextualCondition(HolderSet<MobEffect> effects) implements DialogueRegistry.CompiledCondition<MobEffect> {
        public static final MapCodec<MoonlightContextualConditions.EffectContextualCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        HolderSetCodec.create(
                                Registries.MOB_EFFECT, BuiltInRegistries.MOB_EFFECT.holderByNameCodec(), false
                        ).fieldOf("effects").forGetter(MoonlightContextualConditions.EffectContextualCondition::effects)
                ).apply(instance, MoonlightContextualConditions.EffectContextualCondition::new)
        );

        public static HashMap<MobEffect, ArrayList<DialogueRegistry.DialoguePool>> COMPILED_MAP = new HashMap<>();
        public static HashMap<TagKey<MobEffect>, ArrayList<DialogueRegistry.DialoguePool>> KEY_MAP = new HashMap<>();
        @Override
        public HashMap<MobEffect, ArrayList<DialogueRegistry.DialoguePool>> getMap() {
            return COMPILED_MAP;
        }
        @Override
        public HashMap<TagKey<MobEffect>, ArrayList<DialogueRegistry.DialoguePool>> getTagMap() {
            return KEY_MAP;
        }

        @Override
        public boolean validate(ResourceKey<Registry<DialogueRegistry.DialoguePool>> resourceKey) {
            return resourceKey.equals(NMLRegistries.CONTEXTUAL_DIALOGUE_KEY);
        }

        @Override
        public MapCodec<? extends DialogueRegistry.DialogueCondition> codec() {
            return CODEC;
        }

        @Override
        public HolderSet<MobEffect> getValue() {
            return this.effects();
        }
    }

    public record EquipmentContextualConditional(HolderSet<Item> items) implements DialogueRegistry.CompiledCondition<Item> {
        public static final MapCodec<MoonlightContextualConditions.EquipmentContextualConditional> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        HolderSetCodec.create(
                                Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false
                        ).fieldOf("items").forGetter(MoonlightContextualConditions.EquipmentContextualConditional::items)
                ).apply(instance, MoonlightContextualConditions.EquipmentContextualConditional::new)
        );

        public static HashMap<Item, ArrayList<DialogueRegistry.DialoguePool>> COMPILED_MAP = new HashMap<>();
        public static HashMap<TagKey<Item>, ArrayList<DialogueRegistry.DialoguePool>> KEY_MAP = new HashMap<>();
        @Override
        public HashMap<Item, ArrayList<DialogueRegistry.DialoguePool>> getMap() {
            return COMPILED_MAP;
        }
        @Override
        public HashMap<TagKey<Item>, ArrayList<DialogueRegistry.DialoguePool>> getTagMap() {
            return KEY_MAP;
        }

        @Override
        public boolean validate(ResourceKey<Registry<DialogueRegistry.DialoguePool>> resourceKey) {
            return resourceKey.equals(NMLRegistries.CONTEXTUAL_DIALOGUE_KEY);
        }

        @Override
        public MapCodec<? extends DialogueRegistry.DialogueCondition> codec() {
            return CODEC;
        }

        @Override
        public HolderSet<Item> getValue() {
            return this.items();
        }
    }
}
