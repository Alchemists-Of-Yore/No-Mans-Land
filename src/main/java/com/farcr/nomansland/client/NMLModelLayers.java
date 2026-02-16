package com.farcr.nomansland.client;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.model.*;
import com.farcr.nomansland.client.model.armor.*;
import com.farcr.nomansland.client.model.deer.*;
import com.farcr.nomansland.client.model.goose.*;
import com.farcr.nomansland.client.model.moose.*;
import com.farcr.nomansland.client.model.tortoise.*;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.*;

public class NMLModelLayers {

    //Creatures
    public static final ModelLayerLocation BURIED_LAYER = new ModelLayerLocation(NoMansLand.location("buried"), "main");
    public static final ModelLayerLocation MOOSE_LAYER = new ModelLayerLocation(NoMansLand.location("moose/maple"), "main");
    public static final ModelLayerLocation BASS_LAYER = new ModelLayerLocation(NoMansLand.location("bass"), "main");
    public static final ModelLayerLocation DEER_LAYER = new ModelLayerLocation(NoMansLand.location("deer"), "main");
    public static final ModelLayerLocation GOOSE_LAYER = new ModelLayerLocation(NoMansLand.location("goose"), "main");
    public static final ModelLayerLocation TORTOISE_LAYER = new ModelLayerLocation(NoMansLand.location("tortoise"), "main");

    //Armor
    public static final ModelLayerLocation ANCIENT_BRONZE_MASK_LAYER = new ModelLayerLocation(NoMansLand.location("ancient_bronze_mask"), "main");
    public static final ModelLayerLocation TORTOISE_SHELL_LAYER = new ModelLayerLocation(NoMansLand.location("tortoise_shell"), "main");

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(NMLModelLayers.BURIED_LAYER, BuriedModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.MOOSE_LAYER, MooseModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.BASS_LAYER, BillhookBassModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.DEER_LAYER, DeerModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.GOOSE_LAYER, GooseModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.TORTOISE_LAYER, TortoiseModel::createBodyLayer);

        event.registerLayerDefinition(NMLModelLayers.ANCIENT_BRONZE_MASK_LAYER, AncientBronzeMaskModel::createBodyLayer);
        event.registerLayerDefinition(NMLModelLayers.TORTOISE_SHELL_LAYER, TortoiseShellModel::createBodyLayer);
    }
}