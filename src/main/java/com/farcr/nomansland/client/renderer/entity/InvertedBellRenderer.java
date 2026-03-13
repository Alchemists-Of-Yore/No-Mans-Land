package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import com.farcr.nomansland.common.block.InvertedBellBlock;
import com.farcr.nomansland.common.blockentity.InvertedBellBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;

import java.util.List;

public class InvertedBellRenderer<T extends InvertedBellBlockEntity> implements BlockEntityRenderer<T> {
    public static final ModelResourceLocation BELL_MODEL = ModelResourceLocation.standalone(NoMansLand.location("block/inverted_bell_bell"));
    public static final ModelResourceLocation BEAM_MODEL = ModelResourceLocation.standalone(NoMansLand.location("block/inverted_bell_beam"));

    public InvertedBellRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(T bell, float pt, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        if (bell.isController()) {
            poseStack.pushPose();
            poseStack.rotateAround(
                     Axis.YP.rotationDegrees(180 - bell.getBlockState().getValue(InvertedBellBlock.HORIZONTAL_FACING).toYRot()),
                    0.5f, 0.5f, 0.5f
            );
            poseStack.pushPose();
            // cursed to have a static value affect all bell block entities
            // but there's only ever intended to be at most one on screen and this removes the pain of having a block entity thousands of blocks away ticking on the client
            Quaternionf rotation = InvertedBellClientHandler.instance.getAnimationRotation(pt);
            if (rotation != null) {
                poseStack.rotateAround(rotation, 0.5f, 2f - 4/16f, 0.5f);
            }
            BakedModel model = Minecraft.getInstance().getModelManager().getModel(BELL_MODEL);
            renderModelLists(model, packedLight, packedOverlay, poseStack, multiBufferSource.getBuffer(RenderType.cutout()));
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(0, 1, 0);
            model = Minecraft.getInstance().getModelManager().getModel(BEAM_MODEL);
            renderModelLists(model, packedLight, packedOverlay, poseStack, multiBufferSource.getBuffer(RenderType.cutout()));
            poseStack.popPose();
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
