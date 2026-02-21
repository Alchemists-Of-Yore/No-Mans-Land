package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.rendertype.MoonlightGlowRenderType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    @Inject(method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V", at = @At("HEAD"))
    private void setGlowItemContext(
        ItemStack itemStack, ItemDisplayContext displayContext,
        boolean leftHand, PoseStack poseStack,
        MultiBufferSource bufferSource,
        int combinedLight, int combinedOverlay,
        BakedModel p_model, CallbackInfo callbackInfo
    ) {
        MoonlightGlowRenderType.setContext(itemStack);
    }

    @Inject(method = "getFoilBuffer", at = @At("RETURN"), cancellable = true)
    private static void glowBufferInject(
        MultiBufferSource bufferSource, RenderType renderType,
        boolean isItem, boolean glint, CallbackInfoReturnable<VertexConsumer> cir
    ) {
        cir.setReturnValue(MoonlightGlowRenderType.getConsumer(bufferSource, cir.getReturnValue()));
    }

    @Inject(method = "getFoilBufferDirect", at = @At("RETURN"), cancellable = true)
    private static void glowBufferInjectDirect(
        MultiBufferSource bufferSource, RenderType renderType,
        boolean isItem, boolean glint, CallbackInfoReturnable<VertexConsumer> cir
    ) {
        cir.setReturnValue(MoonlightGlowRenderType.getConsumer(bufferSource, cir.getReturnValue()));
    }
}
