package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    @Inject(method = "stencilFunc", at = @At("HEAD"))
    private static void nml$moonStencil(int func, int ref, int mask, CallbackInfo ci) {
        FriendMoonRenderer.setHighestStencilRef(ref);
    }
}
