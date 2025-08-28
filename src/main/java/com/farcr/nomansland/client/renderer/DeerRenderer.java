package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.deer.DeerAntlersLayer;
import com.farcr.nomansland.client.model.deer.DeerModel;
import com.farcr.nomansland.client.model.deer.DeerPatternLayer;
import com.farcr.nomansland.common.entity.deer.Deer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class DeerRenderer extends MobRenderer<Deer, DeerModel<Deer>> {
    public DeerRenderer(EntityRendererProvider.Context context) {
        super(context, new DeerModel<>(context.bakeLayer(NMLModelLayers.DEER_LAYER)), 0.4F);
        addLayer(new DeerAntlersLayer(this));
        addLayer(new DeerPatternLayer(this));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(Deer deer) {
        return deer.isBaby() ? NoMansLand.location("textures/entity/deer/deer_brown.png") : NoMansLand.location("textures/entity/deer/deer_baby.png");
    }

    @Override
    protected void scale(Deer deer, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(1.1F, 1.1F, 1.1F);
        super.scale(deer, poseStack, partialTickTime);
    }
}
