package com.farcr.nomansland.client;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NMLModelLayers {

    public static final ModelLayerLocation BURIED_LAYER = new ModelLayerLocation(
            NoMansLand.location("buried"), "main");

    public static final ModelLayerLocation MOOSE_LAYER = new ModelLayerLocation(
            NoMansLand.location("moose/maple"), "main");

    public static final ModelLayerLocation BASS_LAYER = new ModelLayerLocation(
            NoMansLand.location("bass"), "main");

    public static final ModelLayerLocation DEER_LAYER = new ModelLayerLocation(
            NoMansLand.location("deer"), "main");

    public static final ModelLayerLocation GOOSE_LAYER = new ModelLayerLocation(
            NoMansLand.location("goose"), "main");

    public static final ModelLayerLocation TORTOISE_LAYER = new ModelLayerLocation(
            NoMansLand.location("tortoise"), "main");
}