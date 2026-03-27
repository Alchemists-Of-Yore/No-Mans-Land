package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.InBedChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InBedChatScreen.class)
public class InBedChatScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void nml$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (ClientDreamRenderer.getInstance().clientIsDreaming())
            ClientDreamRenderer.renderOverlay(guiGraphics, Minecraft.getInstance().getTimer());
    }
}
