package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.handler.InvertedBellClientHandler;
import com.farcr.nomansland.common.block.InvertedBellBlock;
import com.farcr.nomansland.common.blockentity.InvertedBellControllerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;

public class InvertedBellRenderer<T extends InvertedBellControllerBlockEntity> implements BlockEntityRenderer<T> {
    public static final ModelResourceLocation BELL_MODEL = ModelResourceLocation.standalone(NoMansLand.location("block/inverted_bell_bell"));
    public static final ModelResourceLocation BEAM_MODEL = ModelResourceLocation.standalone(NoMansLand.location("block/inverted_bell_beam"));

    private final BlockRenderDispatcher blockRenderer;

    public InvertedBellRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(T bell, float pt, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.rotateAround(
                Axis.YP.rotationDegrees(180 - bell.getBlockState().getValue(InvertedBellBlock.HORIZONTAL_FACING).toYRot()),
                0.5f, 0.5f, 0.5f
        );
        poseStack.pushPose();
        // cursed to have a static value affect all bell block entities
        // but there's only ever intended to be at most one on screen and this removes the pain of having a block entity thousands of blocks away ticking on the client
        Quaternionf rotation = InvertedBellClientHandler.instance.getBellAnimationRotation(pt);
        if (rotation != null) {
            poseStack.rotateAround(rotation, 0.5f, 2f - 4 / 16f, 0.5f);
        }
        BakedModel model = this.blockRenderer.getBlockModelShaper().getModelManager().getModel(BELL_MODEL);
        this.blockRenderer.getModelRenderer().renderModel(poseStack.last(), multiBufferSource.getBuffer(Sheets.solidBlockSheet()), bell.getBlockState(), model,
                1, 1, 1, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0, 1, 0);
        model = this.blockRenderer.getBlockModelShaper().getModelManager().getModel(BEAM_MODEL);
        this.blockRenderer.getModelRenderer().renderModel(poseStack.last(), multiBufferSource.getBuffer(Sheets.solidBlockSheet()), bell.getBlockState(), model,
                1, 1, 1, packedLight, packedOverlay);
        poseStack.popPose();
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(final T blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(2);
    }
}
