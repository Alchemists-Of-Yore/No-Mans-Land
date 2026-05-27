package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.mixin.plugin.annotation.IfModAbsent;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/*
* https://github.com/HaXrDEV/True-Darkness-Refabricated/blob/main/src/client/java/grondag/darkness/Darkness.java#L123
*/
@Mixin(LightTexture.class)
@IfModAbsent("delightmap")
public class LightTextureMixin {
    @Final
    @Shadow
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

    @ModifyArg(method = "updateLightTexture",
               at = @At(
                       value = "INVOKE",
                       target = "Lorg/joml/Vector3f;add(Lorg/joml/Vector3fc;)Lorg/joml/Vector3f;"
               ),
               index = 0
    )
    private Vector3fc nml$friendMoonDarkenSkyLight(Vector3fc v, @Local(ordinal = 0) int skyLight, @Local(ordinal = 2) Vector3f skyLightColor) {
        FriendMoonRenderer.getInstance().modifySkyLightColor(skyLightColor, skyLight);
        return skyLightColor;
    }
    @Inject(method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Vector3f;set(FFF)Lorg/joml/Vector3f;",
                    shift = At.Shift.AFTER
            )
    )
    private void nml$friendMoonDarkenBlockLight(float partialTicks, CallbackInfo ci, @Local(ordinal = 1) int blockLight, @Local(ordinal = 1) Vector3f blockLightColor) {
        FriendMoonRenderer.getInstance().modifyBlockLightColor(blockLightColor, blockLight);
    }
    @ModifyArg(method = "updateLightTexture",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/joml/Vector3f;lerp(Lorg/joml/Vector3fc;F)Lorg/joml/Vector3f;",
                    ordinal = 2
            ),
            index = 1
    )
    private float nml$friendMoonDarkenAmbientLight(float ambientLight) {
        return FriendMoonRenderer.getInstance().modifyAmbientLightFactor(ambientLight);
    }
}
