package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(
        method = "renderArmWithItem",
        at = @At(
            value = "INVOKE",
            shift = At.Shift.AFTER,
            target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V",
            ordinal = 4
        )
    )
    private void nml$wrapBlockingItem(
        AbstractClientPlayer player, float partialTicks,
        float pitch, InteractionHand hand, float swingProgress,
        ItemStack stack, float equippedProgress, PoseStack poseStack,
        MultiBufferSource buffer, int combinedLight, CallbackInfo ci
    ) {
        // not needed in versions higher than 1.21.4 iirc
        if (stack.is(NMLItems.ANCESTRAL_OATH_SWORD)) {
            HumanoidArm humanoidarm = (hand == InteractionHand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();
            int invert = (humanoidarm == HumanoidArm.RIGHT) ? 1 : -1;
            poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) invert * 13.365F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) invert * 78.05F));
        }
    }

    @ModifyVariable(method = "renderArmWithItem", at = @At("HEAD"), argsOnly = true)
    private ItemStack nml$hideItemDuringDream(ItemStack stack) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && ClientDreamRenderer.getInstance().dreamShouldRender())
            return ItemStack.EMPTY;
        return stack;
    }
}
