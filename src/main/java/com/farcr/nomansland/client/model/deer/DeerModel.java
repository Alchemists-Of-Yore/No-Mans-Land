package com.farcr.nomansland.client.model.deer;

import com.farcr.nomansland.common.entity.cervidae.deer.Deer;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DeerModel<T extends Deer> extends QuadrupedModel<T> {
    protected final ModelPart babyHead;
    protected final ModelPart babyBody;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart babyRightHindLeg;
    private final ModelPart babyLeftHindLeg;
    private final ModelPart babyRightFrontLeg;
    private final ModelPart babyLeftFrontLeg;

    public DeerModel(ModelPart root) {
        super(root, false, 0, 0, 1, 1, 0);

        rightFrontLeg = root.getChild("right_front_leg");
        rightHindLeg = root.getChild("right_hind_leg");
        leftFrontLeg = root.getChild("left_front_leg");
        leftHindLeg = root.getChild("left_hind_leg");

        babyBody = root.getChild("baby_body");
        babyHead = root.getChild("baby_head");

        babyRightFrontLeg = root.getChild("baby_right_front_leg");
        babyRightHindLeg = root.getChild("baby_right_hind_leg");
        babyLeftFrontLeg = root.getChild("baby_left_front_leg");
        babyLeftHindLeg = root.getChild("baby_left_hind_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-4, -4, -8.5F, 8, 8, 17), PartPose.offset(0, 9, -0.5F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 23).addBox(-1.5F, -2, -1.5F, 3, 4, 3), PartPose.offset(0, -4, 9));

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2, -11, -4, 4, 11, 5)
                .texOffs(0, 16).addBox(-1.5F, -10, -8, 3, 3, 4), PartPose.offset(0, 9, -7));

        PartDefinition left_antler = head.addOrReplaceChild("left_antler", CubeListBuilder.create(), PartPose.offset(4.3036F, -14.6911F, 0.5648F));

        PartDefinition cube_r1 = left_antler.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 41).addBox(-1, -4, -5.5F, 2, 8, 11), PartPose.offsetAndRotation(0, 0, 0, 0.1309F, 0.3491F, 0.2618F));

        PartDefinition right_antler = head.addOrReplaceChild("right_antler", CubeListBuilder.create(), PartPose.offset(-4.3036F, -14.6911F, 0.5648F));

        PartDefinition cube_r2 = right_antler.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 41).mirror().addBox(-1, -4, -5.5F, 2, 8, 11).mirror(false), PartPose.offsetAndRotation(0, 0, 0, 0.1309F, -0.3491F, -0.2618F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(18, 13).addBox(-1.5F, -1, 0, 3, 2, 0, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-3.0786F, -10.3542F, 0.5029F, 0.1201F, 0.4205F, 0.2875F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(18, 13).mirror().addBox(-1.5F, -1, 0, 3, 2, 0, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(3.0786F, -10.3542F, 0.5029F, 0.1201F, -0.4205F, -0.2875F));

        PartDefinition right_front_leg = partdefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(18, 0).addBox(-1, 0, -1, 2, 11, 2), PartPose.offset(-3, 13, -5));

        PartDefinition right_hind_leg = partdefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(34, 0).addBox(-1, 0, -1, 2, 11, 2), PartPose.offset(-3, 13, 7));

        PartDefinition left_front_leg = partdefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(26, 0).addBox(-1, 0, -1, 2, 11, 2), PartPose.offset(3, 13, -5));

        PartDefinition left_hind_leg = partdefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(42, 0).addBox(-1, 0, -1, 2, 11, 2), PartPose.offset(3, 13, 7));

        PartDefinition baby_body = partdefinition.addOrReplaceChild("baby_body", CubeListBuilder.create().texOffs(0, 0).addBox(-2, -2, -3.5F, 4, 4, 7), PartPose.offset(0, 17, 0));

        PartDefinition baby_tail = baby_body.addOrReplaceChild("baby_tail", CubeListBuilder.create().texOffs(0, 11).addBox(-1, -1, -1, 2, 2, 2), PartPose.offset(0, -2, 3.5F));

        PartDefinition baby_head = partdefinition.addOrReplaceChild("baby_head", CubeListBuilder.create().texOffs(22, 0).addBox(-1.5F, -6, -0.25F, 3, 6, 3)
                .texOffs(8, 11).addBox(-1, -5, -2.25F, 2, 2, 2), PartPose.offset(0, 17, -4.25F));

        PartDefinition baby_right_ear = baby_head.addOrReplaceChild("baby_right_ear", CubeListBuilder.create().texOffs(20, 0).mirror().addBox(-2, -2, 0, 2, 2, 0, new CubeDeformation(0.01F)).mirror(false), PartPose.offsetAndRotation(-1.5F, -4, 2.25F, 0, 0.5236F, 0.1745F));

        PartDefinition baby_left_ear = baby_head.addOrReplaceChild("baby_left_ear", CubeListBuilder.create().texOffs(20, 0).addBox(0, -2, 0, 2, 2, 0, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(1.5F, -4, 2.25F, 0, -0.5236F, -0.1745F));

        PartDefinition baby_right_front_leg = partdefinition.addOrReplaceChild("baby_right_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0, -0.5F, 1, 5, 1), PartPose.offset(-1.5F, 19, -2));

        PartDefinition baby_right_hind_leg = partdefinition.addOrReplaceChild("baby_right_hind_leg", CubeListBuilder.create().texOffs(15, 0).addBox(-0.5F, 0, -0.5F, 1, 5, 1), PartPose.offset(-1.5F, 19, 3));

        PartDefinition baby_left_front_leg = partdefinition.addOrReplaceChild("baby_left_front_leg", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0, -0.5F, 1, 5, 1), PartPose.offset(1.5F, 19, -2));

        PartDefinition baby_left_hind_leg = partdefinition.addOrReplaceChild("baby_left_hind_leg", CubeListBuilder.create().texOffs(15, 0).addBox(-0.5F, 0, -0.5F, 1, 5, 1), PartPose.offset(1.5F, 19, 3));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T deer, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(deer, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        head.xRot /= 3;
        head.yRot /= 3;

        babyHead.xRot = head.xRot / 2;
        babyHead.yRot = head.yRot / 2;

        babyRightFrontLeg.xRot = rightFrontLeg.xRot / 2;
        babyRightHindLeg.xRot = rightHindLeg.xRot / 2;
        babyLeftFrontLeg.xRot = leftFrontLeg.xRot / 2;
        babyLeftHindLeg.xRot = leftHindLeg.xRot / 2;
    }

    @Override
    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
        super.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);

        boolean baby = entity.isBaby();

        head.visible = !baby;
        body.visible = !baby;
        rightHindLeg.visible = !baby;
        leftHindLeg.visible = !baby;
        rightFrontLeg.visible = !baby;
        leftFrontLeg.visible = !baby;

        babyHead.visible = baby;
        babyBody.visible = baby;
        babyRightHindLeg.visible = baby;
        babyLeftHindLeg.visible = baby;
        babyRightFrontLeg.visible = baby;
        babyLeftFrontLeg.visible = baby;
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(head, babyHead);
    }


    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(
                body, rightFrontLeg, rightHindLeg, leftFrontLeg, leftHindLeg,
                babyBody, babyRightFrontLeg, babyRightHindLeg, babyLeftFrontLeg, babyLeftHindLeg);
    }
}
