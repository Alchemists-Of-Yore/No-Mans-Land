package com.farcr.nomansland.common.friend.condition;

import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry.DialogueCondition;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class MoonlightOfferingConditions {
    public record ItemOfferingConditional(Optional<HolderSet<Item>> items) implements DialogueRegistry.CompiledCondition<Item> {
        public static final MapCodec<ItemOfferingConditional> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                HolderSetCodec.create(
                    Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false
                ).optionalFieldOf("items").forGetter(ItemOfferingConditional::items)
            ).apply(instance, ItemOfferingConditional::new)
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
            return resourceKey.equals(NMLRegistries.OFFERING_DIALOGUE_KEY);
        }

        @Override
        public MapCodec<? extends DialogueCondition> codec() {
            return CODEC;
        }

        @Override
        public HolderSet<Item> getValue() {
            if (this.items.isPresent())
                return this.items().get();
            return HolderSet.empty();
        }
    }

    public record EntityOfferingConditional(Optional<HolderSet<EntityType<?>>> entities) implements DialogueRegistry.CompiledCondition<EntityType<?>> {
        public static final MapCodec<EntityOfferingConditional> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                HolderSetCodec.create(
                    Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.holderByNameCodec(), false
                ).optionalFieldOf("entity").forGetter(EntityOfferingConditional::entities)
            ).apply(instance, EntityOfferingConditional::new)
        );

        public static HashMap<EntityType<?>, ArrayList<DialogueRegistry.DialoguePool>> COMPILED_MAP = new HashMap<>();
        public static HashMap<TagKey<EntityType<?>>, ArrayList<DialogueRegistry.DialoguePool>> KEY_MAP = new HashMap<>();
        @Override
        public HashMap<EntityType<?>, ArrayList<DialogueRegistry.DialoguePool>> getMap() {
            return COMPILED_MAP;
        }
        @Override
        public HashMap<TagKey<EntityType<?>>, ArrayList<DialogueRegistry.DialoguePool>> getTagMap() {
            return KEY_MAP;
        }

        @Override
        public boolean validate(ResourceKey<Registry<DialogueRegistry.DialoguePool>> resourceKey) {
            return resourceKey.equals(NMLRegistries.OFFERING_DIALOGUE_KEY);
        }

        @Override
        public MapCodec<? extends DialogueCondition> codec() {
            return CODEC;
        }

        @Override
        public HolderSet<EntityType<?>> getValue() {
            if (this.entities.isPresent())
                return this.entities().get();
            return HolderSet.empty();
        }
    }
}
