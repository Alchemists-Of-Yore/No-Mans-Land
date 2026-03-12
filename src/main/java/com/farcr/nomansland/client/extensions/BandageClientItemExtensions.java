package com.farcr.nomansland.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class BandageClientItemExtensions implements IClientItemExtensions {
    @Override
    public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {

        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0) {
            float flip = arm == HumanoidArm.RIGHT ? 1 : -1;

            renderOffHand(poseStack,player, partialTick, flip, itemInHand);
            renderMainHand(poseStack, player, partialTick, flip, itemInHand);
            animate(poseStack, player, partialTick, flip, itemInHand);

            poseStack.translate(-0.1 * flip, 0.3, 0.3);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            return true;
        } else {
            return false;
        }
    }

    private void renderMainHand(PoseStack poseStack, LocalPlayer player, float partialTick, float flip, ItemStack itemStack) {
        PlayerRenderer playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        int combinedLight = Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(player, partialTick);

        poseStack.pushPose();
        animate(poseStack, player, partialTick, flip, itemStack);
        poseStack.translate(0.3 * flip, -0.5, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-6 * flip));
        if (flip == 1) {
            playerRenderer.renderRightHand(poseStack, buffer, combinedLight, player);
        } else {
            playerRenderer.renderLeftHand(poseStack, buffer, combinedLight, player);
        }
        poseStack.popPose();
    }

    private void renderOffHand(PoseStack poseStack, LocalPlayer player, float partialTick, float flip, ItemStack itemStack) {
        PlayerRenderer playerRenderer = (PlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        MultiBufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        int combinedLight = Minecraft.getInstance().getEntityRenderDispatcher().getPackedLightCoords(player, partialTick);

        float f = player.getUseItemRemainingTicks() - partialTick + 1.0F;
        float progress = 1 - (f / itemStack.getUseDuration(player));
        
        poseStack.pushPose();
        poseStack.translate(-0.1 * flip, -0.66 + Mth.sin((progress * 20) + Mth.PI) / 30, -0.6 + Mth.cos((progress * 20) + Mth.PI) / 30);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-93));
        poseStack.mulPose(Axis.XP.rotationDegrees(-5));
        poseStack.translate(-0.4 * flip, -0.4, 0);
        poseStack.mulPose(Axis.ZP.rotationDegrees(6 * flip));
        if (flip == -1) {
            playerRenderer.renderRightHand(poseStack, buffer, combinedLight, player);
        } else {
            playerRenderer.renderLeftHand(poseStack, buffer, combinedLight, player);
        }
        poseStack.popPose();
    }

    private void animate(PoseStack poseStack, LocalPlayer player, float partialTick, float flip, ItemStack itemStack) {
        float f = player.getUseItemRemainingTicks() - partialTick + 1.0F;
        float progress = 1 - (f / itemStack.getUseDuration(player));

        poseStack.translate(0.4 * flip, -0.7 + Mth.clamp(progress * 10, 0, 1) * 0.2 + Mth.sin(progress * 20) / 7, -0.6 + Mth.cos(progress * 20) / 5);
        poseStack.mulPose(Axis.ZP.rotationDegrees((90 * Mth.clamp(progress * 10, 0, 1) + Mth.sin((progress * 20) + 6) * 8) * flip));
        poseStack.mulPose(Axis.XP.rotationDegrees(-20 + Mth.sin((progress * 20) + 6) * 8));
    }
}
