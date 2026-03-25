package com.farcr.nomansland.client.model.goose;

import com.farcr.nomansland.common.entity.goose.Goose;
import net.minecraft.client.model.AgeableHierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GooseModel<T extends Goose> extends AgeableHierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart rightFlightWing;
    private final ModelPart leftFlightWing;
    private final ModelPart tail;
    private final ModelPart bodyBaby;
    private final ModelPart headBaby;
    private final ModelPart rightLegBaby;
    private final ModelPart leftLegBaby;

    public GooseModel(ModelPart root) {
        super(1, 0);
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.rightLeg = body.getChild("right_leg");
        this.leftLeg = body.getChild("left_leg");
        this.rightWing = body.getChild("right_wing");
        this.leftWing = body.getChild("left_wing");
        this.rightFlightWing = body.getChild("right_flight_wing");
        this.leftFlightWing = body.getChild("left_flight_wing");
        this.tail = body.getChild("tail");
        this.bodyBaby = root.getChild("body_baby");
        this.headBaby = bodyBaby.getChild("head_baby");
        this.rightLegBaby = bodyBaby.getChild("right_leg_baby");
        this.leftLegBaby = bodyBaby.getChild("left_leg_baby");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3, -4.5F, 7, 6, 9), PartPose.offset(0, 17, 0));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 0).addBox(-1.5F, -10, -2, 3, 12, 3), PartPose.offset(0, -1, -3.5F));

        head.addOrReplaceChild("bill", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -1.5F, -3.5F, 2, 3, 2)
                .texOffs(23, 0).addBox(-1, 0.5F, -4.5F, 2, 1, 1), PartPose.offset(0, -8.5F, -0.5F));

        body.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(44, 0).mirror().addBox(-1.5F, 0, -2, 3, 4, 2).mirror(false), PartPose.offset(-2, 3, 0.5F));

        body.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(44, 0).addBox(-1.5F, 0, -2, 3, 4, 2), PartPose.offset(2, 3, 0.5F));

        body.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(46, 0).addBox(0, 0, -4, 1, 5, 8), PartPose.offset(3.5F, -3, -0.5F));

        body.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(46, 0).mirror().addBox(-1, 0, -4, 1, 5, 8).mirror(false), PartPose.offset(-3.5F, -3, -0.5F));

        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(41, 13).addBox(-2.5F, 0, 0, 5, 3, 4), PartPose.offset(0, -3, 4.5F));

        body.addOrReplaceChild("left_flight_wing", CubeListBuilder.create().texOffs(0, 15).addBox(0, -0.5F, -1.5F, 17, 1, 7, new CubeDeformation(0.01F)), PartPose.offset(3.5F, -2.5F, -3));

        body.addOrReplaceChild("right_flight_wing", CubeListBuilder.create().texOffs(0, 15).mirror().addBox(-17, -0.5F, -1.5F, 17, 1, 7, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-3.5F, -2.5F, -3));

        PartDefinition bodyBaby = partdefinition.addOrReplaceChild("body_baby", CubeListBuilder.create().texOffs(0, 0).addBox(-2, -1.5F, -1, 4, 3, 4), PartPose.offset(0, 18.5F, -0.5F));

        bodyBaby.addOrReplaceChild("head_baby", CubeListBuilder.create().texOffs(0, 7).addBox(-1.5F, -2, -3, 3, 3, 3)
                .texOffs(12, 0).addBox(-0.5F, -1, -4, 1, 1, 1), PartPose.offset(0, -0.5F, 0));

        bodyBaby.addOrReplaceChild("right_leg_baby", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0, -1, 1, 2, 1), PartPose.offset(-1, 1.5F, 1));

        bodyBaby.addOrReplaceChild("left_leg_baby", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0, -1, 1, 2, 1), PartPose.offset(1, 1.5F, 1));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(T goose, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        head.xRot = headPitch * 0.017453292F / 3;
        head.yRot = netHeadYaw * 0.017453292F / 3;

        if (!goose.hurtingAnimationState.isStarted()) {
            if (goose.getState() == Goose.State.RUNNING) {
                animateWalk(GooseAnimation.GOOSE_RUN, limbSwing, limbSwingAmount, 2, 3);
            } else {
                if (goose.isInWater()) {
                    animateWalk(GooseAnimation.GOOSE_SWIM, limbSwing, limbSwingAmount, 4, 5);
                } else {
                    animateWalk(GooseAnimation.GOOSE_WALK, limbSwing, limbSwingAmount, 4, 5);
                    animate(goose.intimidatingAnimationState, GooseAnimation.GOOSE_INTIMIDATE, ageInTicks);
                    animate(goose.fallingAnimationState, GooseAnimation.GOOSE_FALL, ageInTicks);
                }
            }
        }

        animate(goose.hurtingAnimationState, GooseAnimation.GOOSE_HURT, ageInTicks);

        boolean baby = goose.isBaby();
        if (baby) {
            rightFlightWing.visible = false;
            leftFlightWing.visible = false;
            rightWing.visible = false;
            leftWing.visible = false;
        } else {
            boolean wingsVisible = goose.showWings();

            rightFlightWing.visible = wingsVisible;
            leftFlightWing.visible = wingsVisible;
            rightWing.visible = !wingsVisible;
            leftWing.visible = !wingsVisible;
        }

        head.visible = !baby;
        rightLeg.visible = !baby;
        leftLeg.visible = !baby;
        tail.visible = !baby;
        body.visible = !baby;
        bodyBaby.visible = baby;

        bodyBaby.xRot = body.xRot;
        bodyBaby.yRot = body.yRot;
        headBaby.xRot = head.xRot / 2;
        headBaby.yRot = head.yRot / 2;
        rightLegBaby.xRot = rightLeg.xRot / 2;
        leftLegBaby.xRot = leftLeg.xRot / 2;
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
