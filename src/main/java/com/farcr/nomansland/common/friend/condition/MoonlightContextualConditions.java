package com.farcr.nomansland.common.friend.condition;

import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
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
import java.util.Optional;

public class MoonlightContextualConditions {
    public record EffectContextualCondition(Optional<HolderSet<MobEffect>> effects) implements DialogueRegistry.CompiledCondition<MobEffect> {
        public static final MapCodec<MoonlightContextualConditions.EffectContextualCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        HolderSetCodec.create(
                                Registries.MOB_EFFECT, BuiltInRegistries.MOB_EFFECT.holderByNameCodec(), false
                        ).optionalFieldOf("effects").forGetter(MoonlightContextualConditions.EffectContextualCondition::effects)
                ).apply(instance, MoonlightContextualConditions.EffectContextualCondition::new)
        );

        public static HashMap<MobEffect, ArrayList<DialoguePool>> COMPILED_MAP = new HashMap<>();
        public static HashMap<TagKey<MobEffect>, ArrayList<DialoguePool>> KEY_MAP = new HashMap<>();
        @Override
        public HashMap<MobEffect, ArrayList<DialoguePool>> getMap() {
            return COMPILED_MAP;
        }
        @Override
        public HashMap<TagKey<MobEffect>, ArrayList<DialoguePool>> getTagMap() {
            return KEY_MAP;
        }

        @Override
        public boolean validate(ResourceKey<Registry<DialoguePool>> resourceKey) {
            return resourceKey.equals(NMLRegistries.CONTEXTUAL_DIALOGUE_KEY);
        }

        @Override
        public MapCodec<? extends DialogueRegistry.DialogueCondition> codec() {
            return CODEC;
        }

        @Override
        public HolderSet<MobEffect> getValue() {
            if (this.effects.isPresent())
                return this.effects().get();
            return HolderSet.empty();
        }
    }

    public record EquipmentContextualConditional(Optional<HolderSet<Item>> items) implements DialogueRegistry.CompiledCondition<Item> {
        public static final MapCodec<MoonlightContextualConditions.EquipmentContextualConditional> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        HolderSetCodec.create(
                                Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false
                        ).optionalFieldOf("items").forGetter(MoonlightContextualConditions.EquipmentContextualConditional::items)
                ).apply(instance, MoonlightContextualConditions.EquipmentContextualConditional::new)
        );

        public static HashMap<Item, ArrayList<DialoguePool>> COMPILED_MAP = new HashMap<>();
        public static HashMap<TagKey<Item>, ArrayList<DialoguePool>> KEY_MAP = new HashMap<>();
        @Override
        public HashMap<Item, ArrayList<DialoguePool>> getMap() {
            return COMPILED_MAP;
        }
        @Override
        public HashMap<TagKey<Item>, ArrayList<DialoguePool>> getTagMap() {
            return KEY_MAP;
        }

        @Override
        public boolean validate(ResourceKey<Registry<DialoguePool>> resourceKey) {
            return resourceKey.equals(NMLRegistries.CONTEXTUAL_DIALOGUE_KEY);
        }

        @Override
        public MapCodec<? extends DialogueRegistry.DialogueCondition> codec() {
            return CODEC;
        }

        @Override
        public HolderSet<Item> getValue() {
            if (this.items.isPresent())
                return this.items().get();
            return HolderSet.empty();
        }
    }
}
