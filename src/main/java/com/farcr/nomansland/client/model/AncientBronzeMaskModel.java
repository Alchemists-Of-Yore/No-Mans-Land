package com.farcr.nomansland.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class AncientBronzeMaskModel<T extends LivingEntity> extends HumanoidModel<T> {
    public AncientBronzeMaskModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");

        head.addOrReplaceChild("mask",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8, new CubeDeformation(0.5F))
                        .texOffs(32, 0).addBox(-4, -8, -4, 8, 8, 8, new CubeDeformation(0.8F)),
                PartPose.ZERO
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.setAllVisible(false);
        this.head.visible = true;
    }
}
