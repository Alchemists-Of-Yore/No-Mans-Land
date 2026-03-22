package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.dreams.DreamManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReceivingLevelScreen.class)
public class ReceivingLevelScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void nml$InjectRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (DreamManager.Client.getInstance().dreamShouldRender()) {
            DreamManager.Client.renderOverlay(guiGraphics, Minecraft.getInstance().getTimer());
            Minecraft.getInstance().mouseHandler.grabMouse();
        }
    }
}
