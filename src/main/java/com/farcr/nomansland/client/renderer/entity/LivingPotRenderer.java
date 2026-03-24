package com.farcr.nomansland.client.renderer.entity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.living_pot.LivingPotBodyLayer;
import com.farcr.nomansland.client.model.living_pot.LivingPotModel;
import com.farcr.nomansland.common.entity.living_pot.LivingPot;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class LivingPotRenderer extends MobRenderer<LivingPot, LivingPotModel<LivingPot>> {

    private static final ResourceLocation TEXTURE = NoMansLand.location("textures/entity/living_pot_legs.png");

    public LivingPotRenderer(EntityRendererProvider.Context context) {
        super(context, new LivingPotModel<>(context.bakeLayer(NMLModelLayers.LIVING_POT_LAYER)), 0.0F);
        addLayer(new LivingPotBodyLayer(this, context.getBlockRenderDispatcher()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(LivingPot pot) {
        return TEXTURE;
    }

    @Override
    protected float getFlipDegrees(LivingPot pot) {
        return 0.0F;
    }
}