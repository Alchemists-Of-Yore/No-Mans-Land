package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.DialogueRenderer;
import com.farcr.nomansland.client.renderer.friend.FriendDreamRenderer;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.friend.dream.DreamManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.gui.GuiLayerManager;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @SuppressWarnings("UnstableApiUsage")
    @Shadow @Final
    private GuiLayerManager layerManager;

    @Inject(method = "renderSleepOverlay", at = @At("HEAD"), cancellable = true)
    private void nml$renderSleepOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        DreamManager.Client clientRenderer = DreamManager.Client.getInstance();
        if (clientRenderer.clientIsDreaming()) {
            clientRenderer.renderOverlay(guiGraphics, deltaTracker);
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        DreamManager.Client clientRenderer = DreamManager.Client.getInstance();
        if (clientRenderer.getDream() != null && clientRenderer.getDream().hideHUD()) {
            clientRenderer.renderOverlay(guiGraphics, deltaTracker);
            ci.cancel();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Inject(method = "<init>", at = @At("TAIL"))
    private void nml$injectDialogueRenderer(Minecraft minecraft, CallbackInfo ci) {
        layerManager.add(NoMansLand.location("dialogue"), DialogueRenderer::render);
    }
}
