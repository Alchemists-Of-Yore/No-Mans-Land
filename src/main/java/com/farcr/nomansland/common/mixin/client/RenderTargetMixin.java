package com.farcr.nomansland.common.mixin.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin {
    @Unique RenderTarget nml$Self = (RenderTarget) (Object) this;

    @Inject(method = "copyDepthFrom", at = @At("HEAD"))
    private void nml$matchStencilFormatBeforeDepthCopy(RenderTarget target, CallbackInfo ci) {
        boolean targetStencil = target.isStencilEnabled();
        boolean isStencil = nml$Self.isStencilEnabled();
        if (targetStencil && !isStencil) nml$Self.enableStencil();
    }
}
