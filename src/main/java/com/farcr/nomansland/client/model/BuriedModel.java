package com.farcr.nomansland.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;

public class BuriedModel<T extends Mob> extends HumanoidModel<T> {
    private static final float ARM_GROUND_REACH = 1.45F;
    private static final float LEG_GROUND_REACH = 1.45F;
    private static final float HAND_GAIT_AMPLITUDE = 0.7F;
    private static final float LEG_GAIT_AMPLITUDE = 0.4F;
    private static final float ARM_SPLAY = 0.25F;
    private static final float LEG_SPLAY = 0.2F;

    public BuriedModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F)
                        .texOffs(2, 0).addBox(-1.0F, 5.0F, -1.0F, 2.0F, 0.0F, 2.0F),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 0).mirror().addBox(-1.0F, -2.0F, -1.0F, 2.0F, 12.0F, 2.0F).mirror(false)
                        .texOffs(-2, 0).mirror().addBox(-1.0F, 5.0F, -1.0F, 2.0F, 0.0F, 2.0F).mirror(false),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(32, 16).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F)
                        .texOffs(2, 2).addBox(-1.0F, 6.0F, -1.0F, 2.0F, 0.0F, 2.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));

        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(32, 0).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 12.0F, 2.0F).mirror(false)
                        .texOffs(-2, 2).mirror().addBox(-1.0F, 6.0F, -1.0F, 2.0F, 0.0F, 2.0F).mirror(false),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        float amount = Math.min(limbSwingAmount, 1.0F);
        float phase = limbSwing * 0.7F;
        float diagonalA = Mth.cos(phase) * amount;
        float diagonalB = Mth.cos(phase + Mth.PI) * amount;

        this.head.xRot = -1.4F + headPitch * ((float) Math.PI / 180.0F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
        this.head.zRot = 0.0F;

        this.rightArm.xRot = ARM_GROUND_REACH + diagonalA * HAND_GAIT_AMPLITUDE;
        this.rightArm.yRot = 0.0F;
        this.rightArm.zRot = -ARM_SPLAY;

        this.leftArm.xRot = ARM_GROUND_REACH + diagonalB * HAND_GAIT_AMPLITUDE;
        this.leftArm.yRot = 0.0F;
        this.leftArm.zRot = ARM_SPLAY;

        this.rightLeg.xRot = LEG_GROUND_REACH + diagonalB * LEG_GAIT_AMPLITUDE;
        this.rightLeg.zRot = -LEG_SPLAY;

        this.leftLeg.xRot = LEG_GROUND_REACH + diagonalA * LEG_GAIT_AMPLITUDE;
        this.leftLeg.zRot = LEG_SPLAY;
    }
}
