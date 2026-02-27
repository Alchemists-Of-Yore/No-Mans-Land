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

        PartDefinition Head = partdefinition.getChild("head");
        PartDefinition RightArm = partdefinition.getChild("right_arm");

        Head.addOrReplaceChild("Fungus_r1", CubeListBuilder.create().texOffs(50, 16).addBox(-3.0F, -4.0F, 0.0F, 6.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
        Head.addOrReplaceChild("Fungus_r2", CubeListBuilder.create().texOffs(50, 16).addBox(-3.0F, -4.0F, 0.0F, 6.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, -1.0F, 0.0F, 0.7854F, 0.0F));

        RightArm.addOrReplaceChild("Fungus_r3", CubeListBuilder.create().texOffs(54, 24).addBox(-4.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 5.0F, 0.0F, 0.0F, 0.3927F, 0.0F));
        RightArm.addOrReplaceChild("Fungus_r4", CubeListBuilder.create().texOffs(54, 20).addBox(-4.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 5.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}