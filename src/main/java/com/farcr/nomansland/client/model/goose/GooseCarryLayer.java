package com.farcr.nomansland.client.model.goose;

import com.farcr.nomansland.common.entity.goose.Goose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GooseCarryLayer extends RenderLayer<Goose, GooseModel<Goose>> {

    private final ItemInHandRenderer itemRenderer;

    public GooseCarryLayer(RenderLayerParent<Goose, GooseModel<Goose>> renderer, ItemInHandRenderer itemRenderer) {
        super(renderer);
        this.itemRenderer = itemRenderer;
    }

    private static boolean isHandheld(Item item) {
        return item instanceof SwordItem || item instanceof DiggerItem || item instanceof TridentItem
                || item instanceof MaceItem || item instanceof ShearsItem || item instanceof FishingRodItem;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, int packedLight, Goose goose,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = goose.getCarriedItem();
        if (stack.isEmpty() || goose.isBaby()) return;

        pose.pushPose();
        getParentModel().translateToBill(pose);
        if (isHandheld(stack.getItem())) {
            pose.translate(0.1F, 0.0F, -0.4F);
            pose.scale(0.7F, 0.7F, 0.7F);
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
            pose.mulPose(Axis.ZP.rotationDegrees(45.0F));
        } else {
            pose.translate(0.0F, 0.0F, -0.3F);
            pose.scale(0.5F, 0.5F, 0.5F);
            pose.mulPose(Axis.XP.rotationDegrees(-90.0F));
        }
        itemRenderer.renderItem(goose, stack, ItemDisplayContext.GROUND, false, pose, buffer, packedLight);
        pose.popPose();
    }
}
