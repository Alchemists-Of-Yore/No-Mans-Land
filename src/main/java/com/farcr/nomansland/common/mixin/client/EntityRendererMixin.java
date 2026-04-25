package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.extension.EntityExtension;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    @ModifyReturnValue(method = "getPackedLightCoords", at = @At("RETURN"))
    private int NML$fullbrightOffering(int original, T entity, float partialTicks) {
        float fade = ((EntityExtension) entity).NML$getInspectionFade(partialTicks);
        if (fade <= 0.001f) return original;

        int block = LightTexture.block(original);
        int sky = LightTexture.sky(original);
        int newBlock = (int) Mth.lerp(fade, block, 15);
        int newSky = (int) Mth.lerp(fade, sky, 15);
        return LightTexture.pack(newBlock, newSky);
    }
}
