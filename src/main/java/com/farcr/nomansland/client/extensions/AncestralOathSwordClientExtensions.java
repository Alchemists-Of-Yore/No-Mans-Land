package com.farcr.nomansland.client.extensions;

import com.farcr.nomansland.common.registry.items.NMLItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class AncestralOathSwordClientExtensions implements IClientItemExtensions {

    /*
    * Probably not good practice to reimplement this, but
    * I want to give the sword a cool shake animation so fuuuuuuuck
    */
    @Override
    public boolean applyForgeHandTransform(
        @NotNull PoseStack poseStack, @NotNull LocalPlayer player,
        @NotNull HumanoidArm arm, @NotNull ItemStack itemInHand,
        float partialTick, float equippedProgress, float swingProgress
    ) {
        ItemInHandRenderer itemInHandRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;
        InteractionHand hand = player.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        int invert = (arm == HumanoidArm.RIGHT) ? 1 : -1;
        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == hand) {
            itemInHandRenderer.applyItemArmTransform(poseStack, arm, equippedProgress);
            poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) invert * 13.365F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) invert * 78.05F));
        } else {
            float f5 = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
            float f6 = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * (float) (Math.PI * 2));
            float f10 = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
            poseStack.translate((float) invert * f5, f6, f10);
            itemInHandRenderer.applyItemArmTransform(poseStack, arm, equippedProgress);
            itemInHandRenderer.applyItemArmAttackTransform(poseStack, arm, swingProgress);
        }
        return true;
    }
}
