package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.bombs.FirebombEntity;
import com.farcr.nomansland.common.entity.bombs.InkBombEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class InkBombRenderer extends ThrowableBombRenderer<InkBombEntity> {

    private static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(NoMansLand.location("entity/ink_bomb"));

    public InkBombRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ModelResourceLocation getModelLocation(InkBombEntity entity) {
        return MODEL;
    }
}