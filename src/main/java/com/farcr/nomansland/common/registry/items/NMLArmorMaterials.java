package com.farcr.nomansland.common.registry.items;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public class NMLArmorMaterials {
    public static final DeferredRegister ARMOR_MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, NoMansLand.MODID);
    public static final Holder<ArmorMaterial> TORTOISE = ARMOR_MATERIALS.register("tortoise", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.CHESTPLATE, NMLConfig.ARMOR_VALUE.get());
            }), 9, SoundEvents.ARMOR_EQUIP_TURTLE, () -> Ingredient.of(NMLItems.STURDY_SCUTE),
            List.of(
                    new ArmorMaterial.Layer(
                            NoMansLand.location("tortoise")
                    ),
                    new ArmorMaterial.Layer(
                            NoMansLand.location("tortoise"), "_overlay", false
                    )
            ), NMLConfig.ARMOR_TOUGHNESS_VALUE.get().floatValue(), NMLConfig.KNOCKBACK_RESISTANCE_VALUE.get().floatValue()));

    public static final Holder<ArmorMaterial> ANCIENT_BRONZE_MASK = ARMOR_MATERIALS.register("ancient_bronze_mask", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 2);
            }), 20, SoundEvents.ARMOR_EQUIP_GOLD, () -> Ingredient.EMPTY,
            List.of(
                    new ArmorMaterial.Layer(
                            NoMansLand.location("nomansland:textures/armor/ancient_bronze_mask")
                    ),
                    new ArmorMaterial.Layer(
                            NoMansLand.location("nomansland:textures/armor/ancient_bronze_mask"), "_overlay", false
                    )
            ), 0.0F, 0.0F));
}