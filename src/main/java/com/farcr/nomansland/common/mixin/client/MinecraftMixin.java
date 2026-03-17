package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.friend.FriendDreamRenderer;
import com.farcr.nomansland.common.friend.dream.DreamManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void nml$OverrideSetScreenDream(Screen guiScreen, CallbackInfo ci) {
        DreamManager.Client renderer = DreamManager.Client.getInstance();
        if (renderer.clientIsDreaming() && guiScreen instanceof InBedChatScreen)
            ci.cancel();
    }
}
