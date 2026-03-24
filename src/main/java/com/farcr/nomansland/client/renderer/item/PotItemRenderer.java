package com.farcr.nomansland.client.renderer.item;

import com.farcr.nomansland.common.block.pots.PotVariant;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.model.data.ModelData;

public class PotItemRenderer extends BlockEntityWithoutLevelRenderer {

    public PotItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        ResourceLocation variantLoc = stack.get(NMLDataComponents.POT_VARIANT);
        if (variantLoc == null) return;

        PotVariant variant = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY)
                .getOptional(ResourceKey.create(NMLRegistries.POT_VARIANT_KEY, variantLoc)).orElse(null);
        if (variant == null) return;

        ModelManager manager = Minecraft.getInstance().getModelManager();
        ModelResourceLocation mrl = ModelResourceLocation.standalone(variant.model().withPrefix("block/"));
        BakedModel model = manager.getModel(mrl);

        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        poseStack.pushPose();

        poseStack.translate(0.5F, 0.5F, 0.5F);
        model.getTransforms().getTransform(displayContext).apply(leftHand, poseStack);
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(Sheets.solidBlockSheet()),
                null, model, 1.0F, 1.0F, 1.0F,
                packedLight, OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY, RenderType.cutout()
        );
        poseStack.popPose();
    }
}
