package com.farcr.nomansland.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class BombItemRenderer extends BlockEntityWithoutLevelRenderer {
    public BombItemRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelSet entityModelSet) {
        super(blockEntityRenderDispatcher, entityModelSet);
    }



    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        Player player = Minecraft.getInstance().player;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        poseStack.popPose();
//        makeItWhite(this.blockRenderer, state, poseStack, buffer, packedLight, i > -1 && i / 5 % 2 == 0);

    }

    public static void makeItWhite(
            BlockRenderDispatcher blockRenderDispatcher, BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight, boolean whiteOverlay
    ) {
        int i;
        if (whiteOverlay) {
            i = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
        } else {
            i = OverlayTexture.NO_OVERLAY;
        }

        blockRenderDispatcher.renderSingleBlock(state, poseStack, buffer, packedLight, i);
    }
}
