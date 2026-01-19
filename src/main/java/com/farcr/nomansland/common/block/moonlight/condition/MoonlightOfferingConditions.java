package com.farcr.nomansland.common.block.moonlight.condition;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.moonlight.DialogueRegistry;
import com.farcr.nomansland.common.block.moonlight.DialogueRegistry.DialogueCondition;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class MoonlightOfferingConditions {
    public enum OfferingType implements StringRepresentable {
        ITEM("item"),
        ENTITY("entity");

        private final String name;
        public static final Codec<OfferingType> CODEC = StringRepresentable.fromEnum(OfferingType::values);

        OfferingType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    public record ItemOfferingConditional(HolderSet<Item> items) implements DialogueRegistry.CompiledCondition<Item> {
        public static final MapCodec<ItemOfferingConditional> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                HolderSetCodec.create(
                    Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false
                ).fieldOf("items").forGetter(ItemOfferingConditional::items)
            ).apply(instance, ItemOfferingConditional::new)
        );

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
