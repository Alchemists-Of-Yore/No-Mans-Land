package com.farcr.nomansland.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class AncientBronzeMaskModel extends LodestoneArmorModel {
    public AncientBronzeMaskModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return createArmorModel((mesh, root, head, body, right_arm, left_arm, leggings, right_legging, left_legging, right_foot, left_foot) -> {
            head.addOrReplaceChild("mask",
                    CubeListBuilder.create()
                            .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8, new CubeDeformation(0.5F))
                            .texOffs(32, 0).addBox(-4, -8, -4, 8, 8, 8, new CubeDeformation(0.8F)),
                    PartPose.ZERO
            );
            return LayerDefinition.create(mesh, 64, 64);
        });
    }
}
