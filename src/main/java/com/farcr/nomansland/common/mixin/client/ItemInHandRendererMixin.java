package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack nml$hideItemDuringDream(ItemStack stack) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && ClientDreamRenderer.getInstance().dreamShouldRender())
            return ItemStack.EMPTY;
        return stack;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;shouldCauseReequipAnimation(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)Z"))
    private boolean nml$noReequipOnDamageChange(ItemStack from, ItemStack to, int slot, Operation<Boolean> original) {
        if (nml$onlyDamageDiffers(from, to)) return false;
        return original.call(from, to, slot);
    }

    private static boolean nml$onlyDamageDiffers(ItemStack from, ItemStack to) {
        if (from.isEmpty() || to.isEmpty()) return false;
        if (!ItemStack.isSameItem(from, to)) return false;
        if (from.getCount() != to.getCount()) return false;
        if (from.getDamageValue() == to.getDamageValue()) return false;
        ItemStack a = from.copy();
        ItemStack b = to.copy();
        a.setDamageValue(0);
        b.setDamageValue(0);
        return ItemStack.matches(a, b);
    }
}
