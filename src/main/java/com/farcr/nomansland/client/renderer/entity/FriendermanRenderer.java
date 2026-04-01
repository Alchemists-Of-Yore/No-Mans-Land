package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.frienderman.FriendermanModel;
import com.farcr.nomansland.common.entity.frienderman.Frienderman;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FriendermanRenderer extends MobRenderer<Frienderman, FriendermanModel> {
    private static final ResourceLocation FRIENDERMAN_LOCATION = NoMansLand.location("textures/entity/frienderman.png");

    public FriendermanRenderer(EntityRendererProvider.Context context) {
        super(context, new FriendermanModel(context.bakeLayer(NMLModelLayers.FRIENDERMAN_LAYER)), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new FriendermanCarriedBlockLayer(this, context.getBlockRenderDispatcher()));
    }

    @Override
    public void render(Frienderman entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        FriendermanModel model = getModel();
        model.carrying = entity.getCarriedBlock() != null;
        model.creepy = false;
        model.hat.visible = !entity.hasExchangedMask();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public Vec3 getRenderOffset(Frienderman entity, float partialTicks) {
        return super.getRenderOffset(entity, partialTicks);
    }

    @Override
    public ResourceLocation getTextureLocation(Frienderman entity) {
        return FRIENDERMAN_LOCATION;
    }

    @OnlyIn(Dist.CLIENT)
    private static class FriendermanCarriedBlockLayer extends RenderLayer<Frienderman, FriendermanModel> {
        private final BlockRenderDispatcher blockRenderer;

        FriendermanCarriedBlockLayer(RenderLayerParent<Frienderman, FriendermanModel> parent, BlockRenderDispatcher blockRenderer) {
            super(parent);
            this.blockRenderer = blockRenderer;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Frienderman entity,
                           float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                           float netHeadYaw, float headPitch) {
            BlockState blockstate = entity.getCarriedBlock();
            if (blockstate != null) {
                poseStack.pushPose();
                poseStack.translate(0.0F, 0.6875F, -0.75F);
                poseStack.mulPose(Axis.XP.rotationDegrees(20.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.translate(0.25F, 0.1875F, 0.25F);
                poseStack.scale(-0.5F, -0.5F, 0.5F);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                this.blockRenderer.renderSingleBlock(blockstate, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
        }
    }
}
