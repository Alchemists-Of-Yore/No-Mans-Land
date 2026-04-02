package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.BuddyModel;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.tazer.mixed_litter.VariantUtil;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

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

        if (entity.isAscending()) {
            float alpha = getAscensionAlpha(entity);
            if (alpha <= 0) return;
            buddyModel.ascensionAlpha = alpha;
        } else {
            buddyModel.ascensionAlpha = 1;
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected RenderType getRenderType(Buddy buddy, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (buddy.isAscending()) return RenderType.itemEntityTranslucentCull(VariantUtil.resolveTexture(buddy, getTextureLocation(buddy)));
        return super.getRenderType(buddy, bodyVisible, translucent, glowing);
    }

    private float getAscensionAlpha(Buddy buddy) {
        int ticks = buddy.getAscensionTicks();
        if (ticks < FriendMoon.ASCENSION_TRANSPARENCY_START)
            return 1;
        float progress = (float) (ticks - FriendMoon.ASCENSION_TRANSPARENCY_START)
            / (FriendMoon.ASCENSION_DURATION - FriendMoon.ASCENSION_TRANSPARENCY_START);
        return Mth.clamp(1 - progress, 0, 1);
    }
}
