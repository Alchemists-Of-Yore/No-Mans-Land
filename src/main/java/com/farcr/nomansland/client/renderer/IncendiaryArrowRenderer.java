package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.IncendiaryArrow;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class IncendiaryArrowRenderer extends ArrowRenderer<IncendiaryArrow> {

    public static final ResourceLocation TEXTURE = NoMansLand.location("textures/entity/incendiary_arrow.png");

    public IncendiaryArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(IncendiaryArrow entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

        float shake = entity.shakeTime - partialTicks;
        if (shake > 0.0F) {
            float shakeRot = -Mth.sin(shake * 3.0F) * shake;
            poseStack.mulPose(Axis.ZP.rotationDegrees(shakeRot));
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90F));

        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(0, -2.5F, 4F);

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(this.getTextureLocation(entity)));
        PoseStack.Pose pose = poseStack.last();

        float U = 1 / 32F;
        float V = 1 / 32F;

        // north
        vertex(pose, consumer, -1, 3.5f, -7, 6 * U, 16 * V, 0, 0, -1, packedLight);
        vertex(pose, consumer,  1, 3.5f, -7, 8 * U, 16 * V, 0, 0, -1, packedLight);
        vertex(pose, consumer,  1, 1.5f, -7, 8 * U, 18 * V, 0, 0, -1, packedLight);
        vertex(pose, consumer, -1, 1.5f, -7, 6 * U, 18 * V, 0, 0, -1, packedLight);

        // south
        vertex(pose, consumer, -1, 1.5f, -1, 14 * U, 18 * V, 0, 0, 1, packedLight);
        vertex(pose, consumer,  1, 1.5f, -1, 16 * U, 18 * V, 0, 0, 1, packedLight);
        vertex(pose, consumer,  1, 3.5f, -1, 16 * U, 16 * V, 0, 0, 1, packedLight);
        vertex(pose, consumer, -1, 3.5f, -1, 14 * U, 16 * V, 0, 0, 1, packedLight);

        // up
        vertex(pose, consumer, -1, 3.5f, -7, 8 * U, 16 * V, 0, 1, 0, packedLight);
        vertex(pose, consumer, -1, 3.5f, -1, 8 * U, 10 * V, 0, 1, 0, packedLight);
        vertex(pose, consumer,  1, 3.5f, -1, 6 * U, 10 * V, 0, 1, 0, packedLight);
        vertex(pose, consumer,  1, 3.5f, -7, 6 * U, 16 * V, 0, 1, 0, packedLight);

        // down
        vertex(pose, consumer, -1, 1.5f, -7, 8 * U, 16 * V, 0, -1, 0, packedLight);
        vertex(pose, consumer,  1, 1.5f, -7, 10 * U, 16 * V, 0, -1, 0, packedLight);
        vertex(pose, consumer,  1, 1.5f, -1, 10 * U, 10 * V, 0, -1, 0, packedLight);
        vertex(pose, consumer, -1, 1.5f, -1, 8 * U, 10 * V, 0, -1, 0, packedLight);

        // west
        vertex(pose, consumer, -1, 1.5f, -7, 8 * U, 18 * V, -1, 0, 0, packedLight);
        vertex(pose, consumer, -1, 1.5f, -1, 14 * U, 18 * V, -1, 0, 0, packedLight);
        vertex(pose, consumer, -1, 3.5f, -1, 14 * U, 16 * V, -1, 0, 0, packedLight);
        vertex(pose, consumer, -1, 3.5f, -7, 8 * U, 16 * V, -1, 0, 0, packedLight);

        // east
        vertex(pose, consumer,  1, 3.5f, -7, 6 * U, 16 * V, 1, 0, 0, packedLight);
        vertex(pose, consumer,  1, 3.5f, -1, 0 * U, 16 * V, 1, 0, 0, packedLight);
        vertex(pose, consumer,  1, 1.5f, -1, 0 * U, 18 * V, 1, 0, 0, packedLight);
        vertex(pose, consumer,  1, 1.5f, -7, 6 * U, 18 * V, 1, 0, 0, packedLight);

        poseStack.popPose();
    }

    public void vertex(PoseStack.Pose pose, VertexConsumer consumer, int x, float y, int z, float u, float v, int normalX, int normalY, int normalZ, int packedLight) {
        consumer.addVertex(pose, (float)x, y, (float)z).setColor(-1).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, (float)normalX, (float)normalZ, (float)normalY);
    }

    @Override
    public ResourceLocation getTextureLocation(IncendiaryArrow incendiaryArrow) {
        return TEXTURE;
    }
}

