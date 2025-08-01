package com.farcr.nomansland.client.model;

import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class TortoiseModel<T extends Tortoise> extends QuadrupedModel<T> {
    public TortoiseModel(ModelPart root) {
        super(root, true, 120.0F, 0.0F, 9.0F, 6.0F, 120);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 37)
                .addBox(-9.0F, -15.0F, -1.0F, 18.0F, 15.0F, 22.0F, new CubeDeformation(0.5F))
                .texOffs(0, 0)
                .addBox(-9.0F, -15.0F, -1.0F, 18.0F, 15.0F, 22.0F, false), PartPose.offset(0.0F, 20.0F, -10.0F));

        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(26, 74)
                .addBox(-2.0F, -2.0F, -6.0F, 4.0F, 4.0F, 8.0F, false)
                .texOffs(80, 0)
                .addBox(-2.0F, -6.0F, -6.0F, 4.0F, 4.0F, 4.0F, false)
                .texOffs(0, 74)
                .addBox(-3.0F, -11.0F, -9.0F, 6.0F, 5.0F, 7.0F, false), PartPose.offset(0.0F, 17.0F, -11.0F));

        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(50, 74)
                .addBox(-2.5F, -4.0F, -2.5F, 5.0F, 8.0F, 5.0F, false), PartPose.offsetAndRotation(8.8536F, 20.0F, -10.8536F, 0.0F, -0.7854F, 0.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(50, 74)
                .addBox(4.5711F, -1.0F, -10.0711F, 5.0F, 8.0F, 5.0F, true), PartPose.offsetAndRotation(-8.5F, 17.0F, -0.5F, 0.0F, 0.7854F, 0.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(50, 74)
                .addBox(-2.5F, -4.0F, -2.5F, 5.0F, 8.0F, 5.0F, true), PartPose.offsetAndRotation(-8.8536F, 20.0F, 10.8536F, 0.0F, 0.7854F, 0.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(50, 74)
                .addBox(-2.5F, -4.0F, -2.5F, 5.0F, 8.0F, 5.0F, false), PartPose.offsetAndRotation(8.8536F, 20.0F, 10.8536F, 0.0F, -0.7854F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.inShell()) {
            for (ModelPart bodyPart : this.bodyParts()) {
                bodyPart.resetPose();
                bodyPart.visible = true;
                this.head.visible = true;
            }
            this.head.resetPose();
        } else {
            for (ModelPart bodyPart : this.bodyParts()) {
                bodyPart.visible = false;
            }
            this.body.visible = true;
            this.head.visible = entity.isSearching();
            this.head.y = 21.0F;
            this.body.y = 25.0F;
        }
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}