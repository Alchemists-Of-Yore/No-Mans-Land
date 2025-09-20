package com.farcr.nomansland.client.model.goose;

import com.farcr.nomansland.common.entity.goose.Goose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GooseModel<T extends Goose> extends HierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart rightFlightWing;
    private final ModelPart leftFlightWing;

    public GooseModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.rightLeg = body.getChild("right_leg");
        this.leftLeg = body.getChild("left_leg");
        this.rightWing = body.getChild("right_wing");
        this.leftWing = body.getChild("left_wing");
        this.rightFlightWing = body.getChild("right_flight_wing");
        this.leftFlightWing = body.getChild("left_flight_wing");
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
                if (goose.isFallFlying()) {
                    animateWalk(GooseAnimation.GOOSE_FALL, limbSwing, limbSwingAmount, 4, 5);
                } else {
                    if (goose.isInWater()) {
                        animateWalk(GooseAnimation.GOOSE_SWIM, limbSwing, limbSwingAmount, 4, 5);
                    }
                    else {
                        animateWalk(GooseAnimation.GOOSE_WALK, limbSwing, limbSwingAmount, 4, 5);
                        animate(goose.intimidatingAnimationState, GooseAnimation.GOOSE_INTIMIDATE, ageInTicks);
                    }
                }
            }
        }

        animate(goose.hurtingAnimationState, GooseAnimation.GOOSE_HURT, ageInTicks);

        boolean visible = goose.showWings();

        rightFlightWing.visible = visible;
        leftFlightWing.visible = visible;
        rightWing.visible = !visible;
        leftWing.visible = !visible;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        root.getChild("body").render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root() {
        return root;
    }
}
