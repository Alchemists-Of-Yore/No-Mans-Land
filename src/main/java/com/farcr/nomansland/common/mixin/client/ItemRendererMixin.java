package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.extensions.AncestralOathSwordClientExtensions;
import com.farcr.nomansland.client.model.utils.BakedModelOpacityWrapper;
import com.farcr.nomansland.client.renderer.rendertype.AncestralGlintRenderType;
import com.farcr.nomansland.client.renderer.rendertype.GlintConsumer;
import com.farcr.nomansland.client.renderer.rendertype.MoonlightGlowRenderType;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Shadow
    public abstract void renderQuadList(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads, ItemStack itemStack, int combinedLight, int combinedOverlay);

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

    @WrapOperation(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderModelLists(Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/item/ItemStack;IILcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;)V"
        )
    )
    private void nml$renderOathGlint(
        ItemRenderer instance, BakedModel model, ItemStack stack,
        int combinedLight, int combinedOverlay, PoseStack poseStack,
        VertexConsumer buffer, Operation<Void> original,
        @Local(argsOnly = true) MultiBufferSource bufferSource
    ) {
        original.call(instance, model, stack, combinedLight, combinedOverlay, poseStack, buffer);
        VertexConsumer consumer = AncestralGlintRenderType.getConsumer(bufferSource, stack);
        if (consumer == null) return;
        if (!(IClientItemExtensions.of(stack) instanceof AncestralOathSwordClientExtensions extensions)) return;

        BakedModelOpacityWrapper modelWrapper = new BakedModelOpacityWrapper(model,
            extensions.getGlintOpacity(stack, Minecraft.getInstance().player));
        original.call(instance, modelWrapper, stack, combinedLight, combinedOverlay, poseStack, consumer);
    }

    @Inject(method = "getFoilBuffer", at = @At("RETURN"), cancellable = true)
    private static void glowBufferInject(
        MultiBufferSource bufferSource, RenderType renderType,
        boolean isItem, boolean glint, CallbackInfoReturnable<VertexConsumer> cir
    ) {
        cir.setReturnValue(GlintConsumer.consume(bufferSource, cir.getReturnValue()));
    }

    @Inject(method = "getFoilBufferDirect", at = @At("RETURN"), cancellable = true)
    private static void glowBufferInjectDirect(
        MultiBufferSource bufferSource, RenderType renderType,
        boolean isItem, boolean glint, CallbackInfoReturnable<VertexConsumer> cir
    ) {
        cir.setReturnValue(GlintConsumer.consume(bufferSource, cir.getReturnValue()));
    }
}
