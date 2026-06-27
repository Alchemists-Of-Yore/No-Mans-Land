package com.farcr.nomansland.client.model;

import com.farcr.nomansland.common.entity.Buried;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class BuriedModel extends HierarchicalModel<Buried> {

    private final ModelPart root;
    private final ModelPart head;

    public BuriedModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("buried").getChild("Head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition buried = root.addOrReplaceChild("buried", CubeListBuilder.create(), PartPose.offset(0, 34, -6));

        buried.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4, -6, -8, 8, 8, 8),
                PartPose.offsetAndRotation(0, -13, 0, 0.0436F, 0, 0.1309F));

        PartDefinition body = buried.addOrReplaceChild("Body",
                CubeListBuilder.create().texOffs(0, 16).addBox(-4, 0, -2, 8, 12, 4),
                PartPose.offsetAndRotation(0, -13, 0, 1.4835F, 0, 0));

        body.addOrReplaceChild("LeftLeg",
                CubeListBuilder.create()
                        .texOffs(32, 0).mirror().addBox(-1, 0, -1, 2, 12, 2).mirror(false)
                        .texOffs(-2, 2).mirror().addBox(-1, 6, -1, 2, 0, 2).mirror(false),
                PartPose.offsetAndRotation(-2, 12, 0, -0.0444F, 0.0852F, 0.1120F));

        body.addOrReplaceChild("RightLeg",
                CubeListBuilder.create()
                        .texOffs(32, 16).addBox(-1, -1, -2, 2, 12, 2)
                        .texOffs(2, 2).mirror().addBox(-1, 5, -2, 2, 0, 2).mirror(false),
                PartPose.offsetAndRotation(2, 13, -1, 0, 0, -0.2182F));

        body.addOrReplaceChild("RightArm",
                CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-1, -1, -1, 2, 12, 2)
                        .texOffs(2, 0).mirror().addBox(-1, 5, -1, 2, 0, 2, new CubeDeformation(0.01F)).mirror(false),
                PartPose.offsetAndRotation(5, 1, 0, -2.7242F, -0.4157F, 0.1073F));

        body.addOrReplaceChild("LeftArm",
                CubeListBuilder.create()
                        .texOffs(40, 0).mirror().addBox(-1, -1, -1, 2, 12, 2).mirror(false)
                        .texOffs(-2, 0).mirror().addBox(-1, 5, -1, 2, 0, 2, new CubeDeformation(-0.01F)).mirror(false),
                PartPose.offsetAndRotation(-5, 1, 0, -2.7213F, 0.0735F, -0.4305F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createArmorLayer(CubeDeformation deformation) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition buried = root.addOrReplaceChild("buried", CubeListBuilder.create(), PartPose.offset(0, 34, -6));

        buried.addOrReplaceChild("Head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4, -6, -8, 8, 8, 8, deformation),
                PartPose.offsetAndRotation(0, -13, 0, 0.0436F, 0, 0.1309F));

        PartDefinition body = buried.addOrReplaceChild("Body", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0, -13, 0, 1.4835F, 0, 0));

        body.addOrReplaceChild("BodyArmor",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4, deformation),
                PartPose.ZERO);

        body.addOrReplaceChild("LeftLeg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2, 0, -2, 4, 12, 4, deformation),
                PartPose.offsetAndRotation(-2, 12, 0, -0.0444F, 0.0852F, 0.1120F));

        body.addOrReplaceChild("RightLeg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2, -1, -3, 4, 12, 4, deformation),
                PartPose.offsetAndRotation(2, 13, -1, 0, 0, -0.2182F));

        body.addOrReplaceChild("RightArm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-2, -1, -2, 4, 12, 4, deformation),
                PartPose.offsetAndRotation(5, 1, 0, -2.7242F, -0.4157F, 0.1073F));

        body.addOrReplaceChild("LeftArm",
                CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-2, -1, -2, 4, 12, 4, deformation),
                PartPose.offsetAndRotation(-5, 1, 0, -2.7213F, 0.0735F, -0.4305F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(Buried entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
