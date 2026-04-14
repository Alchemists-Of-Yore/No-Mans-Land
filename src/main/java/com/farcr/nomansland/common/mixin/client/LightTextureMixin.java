package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.mojang.blaze3d.platform.NativeImage;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


/*
* https://github.com/HaXrDEV/True-Darkness-Refabricated/blob/main/src/client/java/grondag/darkness/Darkness.java#L123
*/
@Mixin(LightTexture.class)
@IfModAbsent("delightmap")
public class LightTextureMixin {
    @Final @Shadow
    private NativeImage lightPixels;

    @Unique
    private static int nml$darken(int c, int block) {
        final float lTarget = block / 16f;
        final float r = (c & 0xFF) / 255f;
        final float g = ((c >> 8) & 0xFF) / 255f;
        final float b = ((c >> 16) & 0xFF) / 255f;
        final float l = nml$luminance(r, g, b);
        final float f = l > 0 ? Math.min(1, lTarget / l) : 0;

        return f == 1f ? c
            : 0xFF000000 | Math.round(f * r * 255) | (Math.round(f * g * 255) << 8)
            | (Math.round(f * b * 255) << 16);
    }

    @Unique
    private static float nml$luminance(float r, float g, float b) {
        return r * 0.2126f + g * 0.7152f + b * 0.0722f;
    }

    @Inject(method = "updateLightTexture",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/texture/DynamicTexture;upload()V"
        )
    )
    private void nml$trueDarknessUpload(float partialTicks, CallbackInfo ci) {
        if (ClientDreamRenderer.getInstance().dreamShouldRender() && lightPixels != null) {
            for (int b = 0; b < 16; b++) {
                for (int s = 0; s < 16; s++) {
                    final int color = nml$darken(lightPixels.getPixelRGBA(b, s), b);
                    lightPixels.setPixelRGBA(b, s, color);
                }
            }
        }
    }

    @Inject(
            method = "calculateDarknessScale",
            at = @At("TAIL"),
            cancellable = true
    )
    private void nml$friendMoonDarken(LivingEntity entity, float gamma, float partialTick, CallbackInfoReturnable<Float> cir) {
        float currentDarknessIntensity = cir.getReturnValue();
        float darknessStrength = FriendMoonRenderer.getInstance().getFriendMoonDarkeningStrength();
        if (darknessStrength > 0.001F) cir.setReturnValue(Math.max(currentDarknessIntensity, darknessStrength));
    }
}
