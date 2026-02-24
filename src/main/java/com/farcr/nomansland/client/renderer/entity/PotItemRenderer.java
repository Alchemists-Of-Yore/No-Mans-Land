package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.common.block.pots.PotSize;
import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PotItemRenderer extends BlockEntityWithoutLevelRenderer {
    private final PotBlockEntity pot = new PotBlockEntity(BlockPos.ZERO, NMLBlocks.ANCIENT_POT.get().defaultBlockState());
    private final PotBlockEntity large_pot = new PotBlockEntity(BlockPos.ZERO, NMLBlocks.LARGE_ANCIENT_POT.get().defaultBlockState());

    public PotItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Item item = stack.getItem();
        Level level = Minecraft.getInstance().level;
        if (item instanceof BlockItem) {
            Block block = ((BlockItem)item).getBlock();
            BlockState blockstate = block.defaultBlockState();
            PotSize size = PotSize.SMALL;
            BlockEntity blockentity = null;
            if (blockstate.is(NMLBlocks.ANCIENT_POT)) {
                this.pot.setLevel(level);
                this.pot.setFromItem(stack);
                blockentity = this.pot;
            } else if (blockstate.is(NMLBlocks.LARGE_ANCIENT_POT)) {
                this.large_pot.setLevel(level);
                this.large_pot.setFromItem(stack);
                blockentity = this.large_pot;
                size = PotSize.LARGE;
            }

            poseStack.pushPose();
            poseStack.translate(0.5F, 0.5F, 0.5F);
            if (displayContext != ItemDisplayContext.GROUND) {
                poseStack.mulPose(Axis.XP.rotationDegrees(30));
                poseStack.mulPose(Axis.YP.rotationDegrees(45));
            }
            if (size == PotSize.SMALL) poseStack.scale(0.8F, 0.8F, 0.8F);
            else poseStack.scale(0.7F, 0.7F, 0.7F);
            if (displayContext.firstPerson()) poseStack.scale(0.8F, 0.8F, 0.8F);
            poseStack.translate(0, 0.05, 0);
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            if (blockentity != null) Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(blockentity, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }
}
