package com.farcr.nomansland.client;

import com.farcr.nomansland.NoMansLand;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class NMLModelLayers {

    public static final ModelLayerLocation PINE_BOAT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "boat/pine"), "main");
    public static final ModelLayerLocation PINE_CHEST_BOAT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "chest_boat/pine"), "main");

    public static final ModelLayerLocation MAPLE_BOAT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "boat/maple"), "main");
    public static final ModelLayerLocation MAPLE_CHEST_BOAT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "chest_boat/maple"), "main");

    public static final ModelLayerLocation WALNUT_BOAT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "boat/walnut"), "main");
    public static final ModelLayerLocation WALNUT_CHEST_BOAT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "chest_boat/walnut"), "main");

    public static final ModelLayerLocation WILLOW_BOAT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "boat/willow"), "main");
    public static final ModelLayerLocation WILLOW_CHEST_BOAT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "chest_boat/willow"), "main");

    public static final ModelLayerLocation BURIED_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "buried"), "main");

    public static final ModelLayerLocation MOOSE_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "moose/maple"), "main");

}
