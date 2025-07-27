package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.bombs.FirebombEntity;
import com.farcr.nomansland.common.entity.bombs.LivingUrnEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class LivingUrnRenderer extends ThrowableBombRenderer<LivingUrnEntity> {

    private static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(NoMansLand.location("entity/living_urn"));

    public LivingUrnRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ModelResourceLocation getModelLocation(LivingUrnEntity entity) {
        return MODEL;
    }
}