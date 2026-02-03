package com.farcr.nomansland.common.friend.condition;

import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry.DialogueCondition;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.HashMap;

public class MoonlightOfferingConditions {
    public record ItemOfferingConditional(HolderSet<Item> items) implements DialogueRegistry.CompiledCondition<Item> {
        public static final MapCodec<ItemOfferingConditional> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                HolderSetCodec.create(
                    Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false
                ).fieldOf("items").forGetter(ItemOfferingConditional::items)
            ).apply(instance, ItemOfferingConditional::new)
        );

        public static HashMap<Item, ArrayList<DialogueRegistry.DialoguePool>> COMPILED_MAP = new HashMap<>();
        @Override
        public HashMap<Item, ArrayList<DialogueRegistry.DialoguePool>> getMap() {
            return COMPILED_MAP;
        }

        @Override
        public MapCodec<? extends DialogueCondition> codec() {
            return CODEC;
        }

        @Override
        public HolderSet<Item> getValue() {
            return this.items();
        }
    }

    public record EntityOfferingConditional(HolderSet<EntityType<?>> entities) implements DialogueRegistry.CompiledCondition<EntityType<?>> {
        public static final MapCodec<EntityOfferingConditional> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                HolderSetCodec.create(
                    Registries.ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.holderByNameCodec(), false
                ).fieldOf("entity").forGetter(EntityOfferingConditional::entities)
            ).apply(instance, EntityOfferingConditional::new)
        );

        public static HashMap<EntityType<?>, ArrayList<DialogueRegistry.DialoguePool>> COMPILED_MAP = new HashMap<>();
        @Override
        public HashMap<EntityType<?>, ArrayList<DialogueRegistry.DialoguePool>> getMap() {
            return COMPILED_MAP;
        }

        @Override
        public MapCodec<? extends DialogueCondition> codec() {
            return CODEC;
        }

        @Override
        public HolderSet<EntityType<?>> getValue() {
            return this.entities();
        }
    }
}
