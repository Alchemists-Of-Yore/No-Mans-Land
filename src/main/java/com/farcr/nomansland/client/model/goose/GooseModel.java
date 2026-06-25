package com.farcr.nomansland.client.model.goose;

import com.farcr.nomansland.common.entity.goose.Goose;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.model.AgeableHierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GooseModel<T extends Goose> extends AgeableHierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart bodyControl;
    private final ModelPart head;
    private final ModelPart bill;
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
        this.bodyControl = body.getChild("body_control");
        this.head = bodyControl.getChild("head");
        this.bill = head.getChild("bill");
        this.rightLeg = body.getChild("right_leg");
        this.leftLeg = body.getChild("left_leg");
        this.rightWing = bodyControl.getChild("right_wing");
        this.leftWing = bodyControl.getChild("left_wing");
        this.rightFlightWing = bodyControl.getChild("right_flight_wing");
        this.leftFlightWing = bodyControl.getChild("left_flight_wing");
        this.tail = bodyControl.getChild("tail");
        this.bodyBaby = root.getChild("body_baby");
        this.headBaby = bodyBaby.getChild("head_baby");
        this.rightLegBaby = bodyBaby.getChild("right_leg_baby");
        this.leftLegBaby = bodyBaby.getChild("left_leg_baby");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0, 17, 0));

        PartDefinition body_control = body.addOrReplaceChild("body_control", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -6, -5, 7, 6, 9), PartPose.offset(0, 3, 0.5F));

        PartDefinition head = body_control.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 0).addBox(-1.5F, -10, -2, 3, 12, 3), PartPose.offset(0, -4, -4));

        head.addOrReplaceChild("bill", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -1.5F, -3.5F, 2, 3, 2)
                .texOffs(23, 0).addBox(-1, 0.5F, -4.5F, 2, 1, 1), PartPose.offset(0, -8.5F, -0.5F));

        body.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(44, 0).mirror().addBox(-1.5F, 0, -2, 3, 4, 2).mirror(false), PartPose.offset(-2, 3, 0.5F));

        body.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(44, 0).addBox(-1.5F, 0, -2, 3, 4, 2), PartPose.offset(2, 3, 0.5F));

        body_control.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(46, 0).addBox(0, -1, -1, 1, 5, 8), PartPose.offset(3.5F, -5, -4));

        body_control.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(46, 0).mirror().addBox(-1, -1, -1, 1, 5, 8).mirror(false), PartPose.offset(-3.5F, -5, -4));

        body_control.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(41, 13).addBox(-2.5F, 0, 0, 5, 3, 4), PartPose.offset(0, -6, 4));

        body_control.addOrReplaceChild("left_flight_wing", CubeListBuilder.create().texOffs(0, 15).addBox(0, -0.5F, -1.5F, 17, 1, 7, new CubeDeformation(0.01F)), PartPose.offset(3.5F, -5.5F, -3.5F));

        body_control.addOrReplaceChild("right_flight_wing", CubeListBuilder.create().texOffs(0, 15).mirror().addBox(-17, -0.5F, -1.5F, 17, 1, 7, new CubeDeformation(0.01F)).mirror(false), PartPose.offset(-3.5F, -5.5F, -3.5F));

        PartDefinition body_baby = partdefinition.addOrReplaceChild("body_baby", CubeListBuilder.create().texOffs(0, 0).addBox(-2, -1.5F, -1, 4, 3, 4), PartPose.offset(0, 18.5F, -0.5F));

        body_baby.addOrReplaceChild("head_baby", CubeListBuilder.create().texOffs(0, 7).addBox(-1.5F, -2, -3, 3, 3, 3)
                .texOffs(12, 0).addBox(-0.5F, -1, -4, 1, 1, 1), PartPose.offset(0, -0.5F, 0));

        body_baby.addOrReplaceChild("right_leg_baby", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-0.5F, 0, -1, 1, 2, 1).mirror(false), PartPose.offset(-1, 1.5F, 1));

        body_baby.addOrReplaceChild("left_leg_baby", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 0, -1, 1, 2, 1), PartPose.offset(1, 1.5F, 1));

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    @Override
    public void setupAnim(T goose, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        Goose.State state = goose.getState();
        boolean flying = state == Goose.State.FLYING;

        boolean headBusy = goose.intimidatingAnimationState.isStarted()
                || goose.chestStealAnimationState.isStarted()
                || goose.grabAnimationState.isStarted()
                || goose.harassAnimationState.isStarted()
                || goose.hurtingAnimationState.isStarted();

        if (!flying && !headBusy) {
            head.xRot = headPitch * 0.017453292F / 3;
            head.yRot = netHeadYaw * 0.017453292F / 3;
        }

        if (flying) {
            animate(goose.flyingAnimationState, flightAnimation(goose), ageInTicks);
        } else if (state == Goose.State.RUNNING) {
            animateWalk(GooseAnimation.GOOSE_RUN, limbSwing, limbSwingAmount, 2, 3);
        } else if (goose.isInWater()) {
            animateWalk(GooseAnimation.GOOSE_SWIM, limbSwing, limbSwingAmount, 4, 5);
        } else {
            animateWalk(GooseAnimation.GOOSE_WALK, limbSwing, limbSwingAmount, 4, 5);
        }

        animate(goose.fallingAnimationState, GooseAnimation.GOOSE_FALL, ageInTicks);
        animate(goose.intimidatingAnimationState, GooseAnimation.GOOSE_INTIMIDATE, ageInTicks);
        animate(goose.chestStealAnimationState, GooseAnimation.GOOSE_CHEST_STEAL, ageInTicks);
        animate(goose.grabAnimationState, GooseAnimation.GOOSE_PLAYER_STEAL, ageInTicks);
        animate(goose.harassAnimationState, GooseAnimation.GOOSE_HARASS, ageInTicks);
        animate(goose.hurtingAnimationState, GooseAnimation.GOOSE_HURT, ageInTicks);

        boolean baby = goose.isBaby();
        body.visible = !baby;
        bodyBaby.visible = baby;

        if (!baby) {
            boolean foldedDriven = goose.harassAnimationState.isStarted()
                    || goose.grabAnimationState.isStarted()
                    || goose.chestStealAnimationState.isStarted()
                    || state == Goose.State.RUMMAGING;
            boolean flightOut = !foldedDriven && goose.showWings();
            rightFlightWing.visible = flightOut;
            leftFlightWing.visible = flightOut;
            rightWing.visible = !flightOut;
            leftWing.visible = !flightOut;
        }

        bodyBaby.xRot = bodyControl.xRot;
        bodyBaby.yRot = bodyControl.yRot;
        headBaby.xRot = head.xRot / 2;
        headBaby.yRot = head.yRot / 2;
        rightLegBaby.xRot = rightLeg.xRot / 2;
        leftLegBaby.xRot = leftLeg.xRot / 2;
    }

    private static AnimationDefinition flightAnimation(Goose goose) {
        return switch (goose.getFlightPose()) {
            case ASCENDING -> GooseAnimation.GOOSE_FLY_UP;
            case GLIDING -> GooseAnimation.GOOSE_GLIDE;
            case FORWARD -> GooseAnimation.GOOSE_FLY_FORWARD;
        };
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public void translateToBill(PoseStack pose) {
        body.translateAndRotate(pose);
        bodyControl.translateAndRotate(pose);
        head.translateAndRotate(pose);
        bill.translateAndRotate(pose);
    }
}
