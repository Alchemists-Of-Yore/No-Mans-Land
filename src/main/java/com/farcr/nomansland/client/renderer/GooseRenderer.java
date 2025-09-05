package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.GooseModel;
import com.farcr.nomansland.common.entity.goose.Goose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GooseRenderer extends MobRenderer<Goose, GooseModel<Goose>> {
    public GooseRenderer(EntityRendererProvider.Context context) {
        super(context, new GooseModel<>(context.bakeLayer(NMLModelLayers.GOOSE_LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Goose goose) {
        return NoMansLand.location("textures/entity/goose/goose_barnacle.png");
    }
}
