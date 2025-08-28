package com.farcr.nomansland.common.item;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

public class TortoiseShellItem extends ArmorItem {
    public TortoiseShellItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        return super.getDefaultAttributeModifiers().withModifierAdded(Attributes.MOVEMENT_SPEED, new AttributeModifier(NoMansLand.location("tortoise_movement_reduction"), NMLConfig.SPEED_REDUCTION_VALUE.get(), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), EquipmentSlotGroup.CHEST);
    }

    /**
     * Called by RenderBiped and RenderPlayer to determine the armor texture that
     * should be use for the currently equipped item. This will only be called on
     * instances of ItemArmor.
     * <p>
     * Returning null from this function will use the default value.
     *
     * @param stack      ItemStack for the equipped armor
     * @param entity     The entity wearing the armor
     * @param slot       The slot the armor is in
     * @param layer      The armor layer
     * @param innerModel Whether the inner model is used
     * @return Path of texture to bind, or null to use default
     */
    @Override
    public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return NoMansLand.location("textures/armor/tortoise_shell.png");
    }
}
