package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.common.entity.bombs.ThrowableBombEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.List;

public abstract class ThrowableBombRenderer<T extends ThrowableBombEntity> extends EntityRenderer<T> {

    public ThrowableBombRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private void renderModelLists(BakedModel bakedModel, int packedLight, int packedOverlay, PoseStack poseStack, VertexConsumer vertexConsumer) {
        RandomSource random = RandomSource.create();
        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            renderQuadList(poseStack, vertexConsumer, bakedModel.getQuads(null, direction, random), packedLight, packedOverlay);
        }

        random.setSeed(42L);
        renderQuadList(poseStack, vertexConsumer, bakedModel.getQuads(null, null, random), packedLight, packedOverlay);
    }

    private void renderQuadList(PoseStack matrixStack, VertexConsumer vertexConsumer, List<BakedQuad> quads, int packedLight, int packedOverlay) {
        PoseStack.Pose pose = matrixStack.last();
        for (BakedQuad bakedQuad : quads) {
            vertexConsumer.putBulkData(pose, bakedQuad, 1.0F, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
        }
    }

    @Override
    public void render(T entity, float yaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffers, int packedLight) {
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(getModelLocation(entity));

        matrixStack.pushPose();
        matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));

        matrixStack.translate(0, entity.getBbHeight() / 2F, 0);
        matrixStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) - entity.getRoll(partialTicks)));
        matrixStack.translate(0, -entity.getBbHeight() / 2F, 0);

        scale(entity, matrixStack, partialTicks);

        matrixStack.translate(-0.5, 0.0, -0.5);
        renderModelLists(model, packedLight, OverlayTexture.pack(OverlayTexture.u(getWhiteOverlayProgress(entity, partialTicks)), 10), matrixStack, buffers.getBuffer(RenderType.entityCutout(getTextureLocation(entity))));

        matrixStack.popPose();

        super.render(entity, yaw, partialTicks, matrixStack, buffers, packedLight);
    }

    protected void scale(T entity, PoseStack poseStack, float partialTicks) {
        float f = entity.getSwelling(partialTicks);
        float f1 = 1.0F + Mth.sin(f * 100.0F) * f * 0.01F;
        f = Mth.clamp(f, 0.0F, 1.0F);
        f *= f;
        f *= f;
        float f2 = (1.0F + f * 0.5F) * f1;
        float f3 = (1.0F + f * 0.2F) / f1;
        poseStack.scale(f2, f3, f2);
    }

    protected float getWhiteOverlayProgress(T entity, float partialTicks) {
        float f = entity.getSwelling(partialTicks);
        return (int)(f * 5.0F) % 2 == 0 ? 0.0F : Mth.clamp(f, 0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    public abstract ModelResourceLocation getModelLocation(T entity);
}