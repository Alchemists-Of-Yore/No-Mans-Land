package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
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
}
