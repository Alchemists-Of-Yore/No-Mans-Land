package com.farcr.nomansland.client.model;

import com.farcr.nomansland.common.entity.buddy.Buddy;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class BuddyModel<T extends Buddy> extends PlayerModel<T> {

    public BuddyModel(ModelPart root) {
        super(root, true);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = PlayerModel.createMesh(CubeDeformation.NONE, true);
        PartDefinition partdefinition = meshdefinition.getRoot();
        // Mushrooms and such would be added here
        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}