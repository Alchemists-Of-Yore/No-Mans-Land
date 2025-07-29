package com.farcr.nomansland.client.renderer;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.IncendiaryArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class IncendiaryArrowRenderer extends ArrowRenderer<IncendiaryArrow> {

    public static final ResourceLocation TEXTURE = NoMansLand.location("textures/entity/incendiary_arrow.png");

    public IncendiaryArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(IncendiaryArrow incendiaryArrow) {
        return TEXTURE;
    }
}
