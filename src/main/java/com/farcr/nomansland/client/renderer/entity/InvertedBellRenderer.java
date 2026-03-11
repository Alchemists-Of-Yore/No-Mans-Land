package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.blockentity.InvertedBellBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;

import java.util.List;

public class InvertedBellRenderer<T extends InvertedBellBlockEntity> implements BlockEntityRenderer<T> {
    public static final ResourceLocation TEXTURE = NoMansLand.location("textures/block/inverted_bell_temp.png");
    public static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(NoMansLand.location("block/inverted_bell_temp"));

    public InvertedBellRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T bell, float pt, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        if (bell.isController()) {
            poseStack.pushPose();

            Quaternionf rotation = bell.getAnimationRotation(pt);
            if (rotation != null) {
                poseStack.rotateAround(rotation, 0.5f, 2f, 0.5f);
            }

            BakedModel model = Minecraft.getInstance().getModelManager().getModel(MODEL);
            renderModelLists(model, packedLight, packedOverlay, poseStack, multiBufferSource.getBuffer(RenderType.cutout()));

            poseStack.popPose();
        }
    }

    private static void renderModelLists(BakedModel bakedModel, int packedLight, int packedOverlay, PoseStack poseStack, VertexConsumer vertexConsumer) {
        RandomSource random = RandomSource.create();
        for (Direction direction : Direction.values()) {
            random.setSeed(42L);
            renderQuadList(poseStack, vertexConsumer, bakedModel.getQuads(null, direction, random), packedLight, packedOverlay);
        }

        random.setSeed(42L);
        renderQuadList(poseStack, vertexConsumer, bakedModel.getQuads(null, null, random), packedLight, packedOverlay);
    }

    private static void renderQuadList(PoseStack matrixStack, VertexConsumer vertexConsumer, List<BakedQuad> quads, int packedLight, int packedOverlay) {
        PoseStack.Pose pose = matrixStack.last();
        for (BakedQuad bakedQuad : quads) {
            vertexConsumer.putBulkData(pose, bakedQuad, 1.0F, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
        }
    }

    @Override
    public AABB getRenderBoundingBox(final T blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(2);
    }
}
