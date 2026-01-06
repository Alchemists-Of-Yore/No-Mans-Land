package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.NMLModelLayers;
import com.farcr.nomansland.client.model.moose.MooseAntlersLayer;
import com.farcr.nomansland.client.model.moose.MooseModel;
import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MooseRenderer extends MobRenderer<Moose, MooseModel<Moose>> {

    public MooseRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new MooseModel<>(pContext.bakeLayer(NMLModelLayers.MOOSE_LAYER)), 1f);
        this.addLayer(new MooseAntlersLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Moose pEntity) {
        return NoMansLand.location("textures/entity/moose/moose_brown.png");
    }
}