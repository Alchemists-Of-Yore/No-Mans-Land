package com.farcr.nomansland.client.renderer;
//    TODO: BURIED
//import com.farcr.nomansland.client.NMLModelLayers;
//import com.farcr.nomansland.client.models.BuriedModel;
//import com.farcr.nomansland.core.NoMansLand;
//import com.farcr.nomansland.core.content.entity.BuriedEntity;
//import com.farcr.nomansland.core.content.entity.variant.BuriedVariant;
//import com.google.common.collect.Maps;
//import net.minecraft.Util;
//import net.minecraft.client.renderer.entity.EntityRendererProvider;
//import net.minecraft.client.renderer.entity.MobRenderer;
//import net.minecraft.resources.ResourceLocation;
//
//import java.util.Map;
//
//public class BuriedRenderer extends MobRenderer<BuriedEntity, BuriedModel<BuriedEntity>> {
//    public static final Map<BuriedVariant, ResourceLocation> LOCATION_BY_VARIANT =
//            Util.make(Maps.newEnumMap(BuriedVariant.class), (p_114874_) -> {
//                p_114874_.put(BuriedVariant.ZERO,
//                        ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "textures/entity/buried/buried_0.png"));
//                p_114874_.put(BuriedVariant.ONE,
//                        ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "textures/entity/buried/buried_1.png"));
//                p_114874_.put(BuriedVariant.TWO,
//                        ResourceLocation.fromNamespaceAndPath(NoMansLand.MODID, "textures/entity/buried/buried_2.png"));
//            });
//
//    public BuriedRenderer(EntityRendererProvider.Context pContext) {
//        super(pContext, new BuriedModel(pContext.bakeLayer(NMLModelLayers.BURIED_LAYER)), 0.5f);
//    }
//
//    @Override
//    public ResourceLocation getTextureLocation(BuriedEntity instance) {
//        return LOCATION_BY_VARIANT.get(instance.getVariant());
//    }
//}