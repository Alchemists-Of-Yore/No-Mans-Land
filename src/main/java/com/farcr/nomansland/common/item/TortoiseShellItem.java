package com.farcr.nomansland.common.item;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

public class TortoiseShellItem extends ArmorItem {
    public TortoiseShellItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return super.getDefaultAttributeModifiers().withModifierAdded(Attributes.MOVEMENT_SPEED, new AttributeModifier(NoMansLand.location("tortoise_movement_reduction"), NMLConfig.SPEED_REDUCTION_VALUE.get(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), EquipmentSlotGroup.CHEST);
    }

    /**
     * Called each tick as long the item is in a player's inventory. Used by maps to check if it's in a player's hand and update its contents.
     *
     * @param stack
     * @param level
     * @param entity
     * @param slotId
     * @param isSelected
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }


}
