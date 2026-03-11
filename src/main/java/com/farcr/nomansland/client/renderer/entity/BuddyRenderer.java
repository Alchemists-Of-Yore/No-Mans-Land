package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.BuddyModel;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BuddyRenderer extends HumanoidMobRenderer<Buddy, BuddyModel<Buddy>> {
    public BuddyRenderer(EntityRendererProvider.Context context) {
        super(context, new BuddyModel<>(context.bakeLayer(NMLModelLayers.BUDDY_LAYER)), .5f);
    }

    @Override
    public ResourceLocation getTextureLocation(Buddy buddy) {
        return NoMansLand.location("textures/entity/buddy/red.png");
    }

    @Override
    public void render(Buddy entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BuddyModel<Buddy> buddyModel = this.getModel();
        buddyModel.crouching = entity.isCrouching();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
