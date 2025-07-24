package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.deer.DeerAntlersLayer;
import com.farcr.nomansland.client.model.moose.MooseAntlersLayer;
import com.farcr.nomansland.client.model.moose.MooseModel;
import com.farcr.nomansland.common.entity.Moose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

// todo: texture variants
public class MooseRenderer extends MobRenderer<Moose, MooseModel<Moose>> {
    private static final ResourceLocation TEXTURE = NoMansLand.location("textures/entity/moose/moose_brown.png");

    public MooseRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new MooseModel<>(pContext.bakeLayer(NMLModelLayers.MOOSE_LAYER)), 1f);
        this.addLayer(new MooseAntlersLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Moose pEntity) {
        return TEXTURE;
    }
}