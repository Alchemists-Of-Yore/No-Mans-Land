package com.farcr.nomansland.client.extensions;

import com.farcr.nomansland.common.registry.items.NMLItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class AncestralOathSwordClientExtensions implements IClientItemExtensions {

    private float trackedEquipProgress = 1f;

    /*
    * Probably not good practice to reimplement this, but
    * I want to give the sword a cool shake animation so fuuuuuuuck
    */
    @Override
    public boolean applyForgeHandTransform(
        @NotNull PoseStack poseStack, @NotNull LocalPlayer player,
        @NotNull HumanoidArm arm, @NotNull ItemStack itemInHand,
        float partialTick, float equipProcess, float swingProcess
    ) {
        ItemInHandRenderer itemInHandRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;
        if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0) {
            itemInHandRenderer.applyItemArmTransform(poseStack, arm, equipProcess);
            int invert = (arm == HumanoidArm.RIGHT) ? 1 : -1;
            poseStack.mulPose(Axis.XP.rotationDegrees(-102.25F));
            poseStack.mulPose(Axis.YP.rotationDegrees((float) invert * 13.365F));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) invert * 78.05F));
        } else itemInHandRenderer.applyItemArmTransform(poseStack, arm, equipProcess);
        return true;
    }
}
