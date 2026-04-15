package com.farcr.nomansland.client.model.moose;

import com.farcr.nomansland.client.model.utils.AnimUtil;
import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import net.minecraft.client.model.AgeableHierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

import java.util.List;

public class MooseModel<T extends Moose> extends AgeableHierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart left_ear;
    private final ModelPart right_ear;
    private final ModelPart dewlap;
    private final ModelPart saddle_head;
    private final ModelPart saddle_reins;
    private final ModelPart upperbody;
    private final ModelPart lowerbody;
    private final ModelPart saddle_lowerbody;
    private final ModelPart saddle_upperbody;
    private final ModelPart legs;
    private final ModelPart right_leg_back;
    private final ModelPart left_leg_back;
    private final ModelPart right_leg;
    private final ModelPart left_leg;

    protected final List<ModelPart> saddleParts;

    public MooseModel(ModelPart root) {
        super(0.5F, 24.0F);
        this.root = root;
        this.head = root.getChild("head");
        this.left_ear = this.head.getChild("left_ear");
        this.right_ear = this.head.getChild("right_ear");
        this.dewlap = this.head.getChild("dewlap");
        this.saddle_head = this.head.getChild("saddle_head");
        this.saddle_reins = this.head.getChild("saddle_reins");
        this.upperbody = root.getChild("upperbody");
        this.lowerbody = this.upperbody.getChild("lowerbody");
        this.saddle_lowerbody = this.lowerbody.getChild("saddle_lowerbody");
        this.saddle_upperbody = this.upperbody.getChild("saddle_upperbody");
        this.legs = root.getChild("legs");
        this.right_leg_back = this.legs.getChild("right_leg_back");
        this.left_leg_back = this.legs.getChild("left_leg_back");
        this.right_leg = this.legs.getChild("right_leg");
        this.left_leg = this.legs.getChild("left_leg");

        saddleParts = List.of(
                saddle_head,
                saddle_reins,
                saddle_lowerbody,
                saddle_upperbody
        );
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(26, 65).addBox(-3.5F, -3.0F, -4.0F, 7.0F, 9.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(58, 0).addBox(-3.5F, -3.0F, -13.0F, 7.0F, 8.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.75F, -16.0F));

        PartDefinition rightantler_r1 = head.addOrReplaceChild("rightantler_r1", CubeListBuilder.create().texOffs(44, 19).mirror().addBox(0.0F, -8.0F, 0.0F, 8.0F, 8.0F, 14.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -2.75F, -12.5F, 0.5824F, 0.0555F, -1.1258F));

        PartDefinition rightantlerconnection_r1 = head.addOrReplaceChild("rightantlerconnection_r1", CubeListBuilder.create().texOffs(37, 41).mirror().addBox(-3.761F, -0.0463F, 0.1866F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-1.0F, -3.0F, -11.0F, 0.0117F, -0.6323F, 0.3834F));

        PartDefinition snout_r1 = head.addOrReplaceChild("snout_r1", CubeListBuilder.create().texOffs(0, 65).addBox(-2.5F, -0.0342F, -7.5221F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, -13.0F, 0.4363F, 0.0F, 0.0F));

        PartDefinition leftantler_r1 = head.addOrReplaceChild("leftantler_r1", CubeListBuilder.create().texOffs(44, 19).addBox(-8.0F, -8.0F, 0.0F, 8.0F, 8.0F, 14.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(2.0F, -2.75F, -12.5F, 0.5824F, -0.0555F, 1.1258F));

        PartDefinition leftantlerconnection_r1 = head.addOrReplaceChild("leftantlerconnection_r1", CubeListBuilder.create().texOffs(37, 41).addBox(-0.239F, -0.0463F, 0.1866F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(1.0F, -3.0F, -11.0F, 0.0111F, 0.6399F, -0.3842F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(10, 41).addBox(-1.3473F, -4.4696F, -0.5F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7605F, -2.4772F, -7.5F, 0.0F, 0.0F, 0.1745F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(10, 41).mirror().addBox(-0.6527F, -4.4696F, -0.5F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.7605F, -2.4772F, -7.5F, 0.0F, 0.0F, -0.1745F));

        PartDefinition dewlap = head.addOrReplaceChild("dewlap", CubeListBuilder.create().texOffs(0, 25).addBox(0.0F, 0.0F, -6.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(52, 66).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, -6.0F));

        PartDefinition saddle_head = head.addOrReplaceChild("saddle_head", CubeListBuilder.create().texOffs(0, 93).addBox(-4.0F, -3.0F, -13.0F, 8.0F, 8.0F, 13.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bit_r1 = saddle_head.addOrReplaceChild("bit_r1", CubeListBuilder.create().texOffs(0, 80).addBox(-2.5F, -2.0F, -3.0F, 5.0F, 6.0F, 7.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(0.0F, 1.75F, -16.0F, 0.4363F, 0.0F, 0.0F));

        PartDefinition saddle_reins = head.addOrReplaceChild("saddle_reins", CubeListBuilder.create().texOffs(19, 94).addBox(-4.5F, -11.0F, -0.5F, 9.0F, 11.0F, 23.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -5.5F));

        PartDefinition upperbody = partdefinition.addOrReplaceChild("upperbody", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, 0.0F, 0.0F, 14.0F, 18.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, -16.0F));

        PartDefinition lowerbody = upperbody.addOrReplaceChild("lowerbody", CubeListBuilder.create().texOffs(0, 33).addBox(-5.0F, -15.0F, 0.0F, 10.0F, 15.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 18.0F, 15.0F));

        PartDefinition tail_r1 = lowerbody.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(0, 41).addBox(-1.0F, -16.5445F, 14.6003F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.0F, -5.0F, -0.829F, 0.0F, 0.0F));

        PartDefinition saddle_lowerbody = lowerbody.addOrReplaceChild("saddle_lowerbody", CubeListBuilder.create().texOffs(74, 34).addBox(-5.0F, -15.0F, 0.0F, 10.0F, 9.0F, 17.0F, new CubeDeformation(0.4F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition saddle_upperbody = upperbody.addOrReplaceChild("saddle_upperbody", CubeListBuilder.create().texOffs(70, 80).addBox(-7.0F, 0.0F, 0.0F, 14.0F, 18.0F, 15.0F, new CubeDeformation(0.4F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition legs = partdefinition.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, -0.5F));

        PartDefinition right_leg_back = legs.addOrReplaceChild("right_leg_back", CubeListBuilder.create().texOffs(68, 60).addBox(-1.0F, -3.0F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, 11.0F, -1.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, 1.0F, 10.5F));

        PartDefinition left_leg_back = legs.addOrReplaceChild("left_leg_back", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.0F, 11.0F, -1.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(68, 60).mirror().addBox(-2.0F, -3.0F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(6.0F, 1.0F, 10.5F));

        PartDefinition right_leg = legs.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(54, 41).mirror().addBox(-1.0F, -6.0F, -3.0F, 4.0F, 19.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(43, 0).addBox(0.0F, 13.0F, -2.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.0F, -1.0F, -10.5F));

        PartDefinition left_leg = legs.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(54, 41).addBox(-3.0F, -6.0F, -3.0F, 4.0F, 19.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(43, 0).mirror().addBox(-3.0F, 13.0F, -2.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(7.0F, -1.0F, -10.5F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    public void setSaddleVisibility(boolean visible) {
        for (ModelPart saddlePart : saddleParts) {
            saddlePart.visible = visible;
        }
    }

    @Override
    public void setupAnim(Moose moose, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        boolean isMounted = moose.isSaddled() && moose.isVehicle();
        setSaddleVisibility(false);
        if (isMounted) {
            animateWalk(MooseAnimations.MOUNTED, limbSwing, limbSwingAmount, 1.2f, 1f);
        }
        else {
            idleAnimations(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
        animate(moose.shakeAnimationState, MooseAnimations.SHAKE, ageInTicks);
        animate(moose.stompAnimationState, MooseAnimations.STOMPING, ageInTicks);
        animate(moose.attackAnimationState, MooseAnimations.ATTACK, ageInTicks);

        animate(moose.chargedAttackStartAnimationState, MooseAnimations.CHARGING_START, ageInTicks);
        animate(moose.chargedAttackHoldAnimationState, MooseAnimations.CHARGING_LOOP, ageInTicks);
        animate(moose.chargedAttackEndAnimationState, MooseAnimations.UPPERCUT, ageInTicks);
    }

    public void idleAnimations(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // IDLE ANIMATION
        {
            float idleTime = ageInTicks * 0.01F;
            float idlePower = 1.0F;

            left_ear.zRot += AnimUtil.wave(idleTime) * idlePower * 0.03F;
            left_ear.xRot += (AnimUtil.wave(idleTime * 0.5F)/2.0F+0.7F) * idlePower * 0.1F;
            right_ear.zRot += AnimUtil.wave(idleTime * 0.92F) * idlePower * 0.03F;
            right_ear.xRot += (AnimUtil.wave(idleTime * 0.5F + 0.2F)/2.0F+0.7F) * idlePower * 0.1F;

            dewlap.zRot += AnimUtil.wave(idleTime) * idlePower * 0.03F;
            dewlap.xRot += AnimUtil.wave(idleTime * 0.93F) * idlePower * 0.01F;

//            neck.y += AnimUtil.wave(idleTime) * idlePower * 0.1F;
            head.y += AnimUtil.wave(idleTime - 0.2F) * idlePower * 0.05F;
            float bodyYOffset = AnimUtil.wave(idleTime + 0.2F) * idlePower * 0.1F;
            upperbody.y += bodyYOffset;
            left_leg.y -= bodyYOffset;
            right_leg.y -= bodyYOffset;
            left_leg_back.y -= bodyYOffset;
            right_leg_back.y -= bodyYOffset;
        }
        // LOOK ANIMATION
        {
            this.head.xRot += headPitch * 0.6F * Mth.DEG_TO_RAD;
            this.head.yRot += netHeadYaw * 0.6F * Mth.DEG_TO_RAD;
//            this.neck.xRot += headPitch * 0.4F * Mth.DEG_TO_RAD;
//            this.neck.yRot += netHeadYaw * 0.4F * Mth.DEG_TO_RAD;

            float upperBodyXRot = headPitch * 0.1F * Mth.DEG_TO_RAD;
            float upperBodyYRot = netHeadYaw * 0.05F * Mth.DEG_TO_RAD;
            this.upperbody.xRot += upperBodyXRot;
            this.upperbody.yRot += upperBodyYRot;
            left_leg.xRot -= upperBodyXRot;
            right_leg.xRot -= upperBodyXRot;
            left_leg_back.xRot -= upperBodyXRot;
            right_leg_back.xRot -= upperBodyXRot;
            left_leg.yRot -= upperBodyYRot;
            right_leg.yRot -= upperBodyYRot;
            left_leg_back.yRot -= upperBodyYRot;
            right_leg_back.yRot -= upperBodyYRot;

            this.dewlap.xRot -= Math.min(headPitch * Mth.DEG_TO_RAD, 0.1F);
        }
        // LOCOMOTION
        float runWeight = Math.clamp((limbSwingAmount - 0.8F) * 8, 0, 1);
        float walkWeight = 1 - runWeight;

        // WALK ANIMATION
        {
            float walkTime = limbSwing * 0.2F;
            float walkPower = Math.min(limbSwingAmount * 3.5F, 0.8F) * walkWeight;
            float bodyYOffset =  AnimUtil.wave(walkTime * 2) * walkPower * -0.3F;
            upperbody.y += bodyYOffset;
            upperbody.z += AnimUtil.wave(walkTime * 2 + 0.1F) * walkPower * 0.3F;

            left_leg.y -= bodyYOffset;
            right_leg.y -= bodyYOffset;
            left_leg_back.y -= bodyYOffset;
            right_leg_back.y -= bodyYOffset;

            left_leg.xRot += AnimUtil.wave(walkTime + 0.5F - 0.1F) * walkPower * 0.2F;
            left_leg.y -= Math.max(AnimUtil.wave(walkTime), 0) * walkPower * 4;
            left_leg.z -= AnimUtil.wave(walkTime + 0.75F) * walkPower * 3;

            right_leg.xRot += AnimUtil.wave(walkTime - 0.1F) * walkPower * 0.2F;
            right_leg.y -= Math.max(AnimUtil.wave(walkTime + 0.5F), 0) * walkPower * 4;
            right_leg.z -= AnimUtil.wave(walkTime + 0.25F) * walkPower * 3;

            walkTime += 0.1F;
            left_leg_back.xRot += AnimUtil.wave(walkTime - 0.1F) * walkPower * 0.1F;
            left_leg_back.y -= Math.max(AnimUtil.wave(walkTime + 0.5F), 0) * walkPower * 2;
            left_leg_back.z -= AnimUtil.wave(walkTime + 0.25F) * walkPower * 3;
            walkTime -= 0.1F;

            right_leg_back.xRot += AnimUtil.wave(walkTime + 0.5F - 0.1F) * walkPower * 0.1F;
            right_leg_back.y -= Math.max(AnimUtil.wave(walkTime), 0) * walkPower * 2;
            right_leg_back.z -= AnimUtil.wave(walkTime + 0.75F) * walkPower * 3;

//            neck.y += AnimUtil.wave(walkTime * 2 - 0.2F) * walkPower * -0.2F;
//            neck.xRot += AnimUtil.wave(walkTime * 2) * walkPower * -0.005F;
            head.y += AnimUtil.wave(walkTime * 2 - 0.4F) * walkPower * -0.1F;

//            tail.y += AnimUtil.wave(walkTime * 2 + 0.2F) * walkPower * -0.1F;

            float earZRot = (AnimUtil.wave(walkTime * 2 - 0.4F)/2.0F+0.5F) * walkPower * 0.1F;
            left_ear.zRot  -= earZRot;
            right_ear.zRot += earZRot;
            float earXRot = (AnimUtil.wave(walkTime * 2 - 0.7F)/2.0F+0.5F) * walkPower * 0.1F;
            left_ear.xRot  += earXRot;
            right_ear.xRot += earXRot;
        }
        // RUN ANIMATION
        {
            float runTime = limbSwing * 0.1F;
            float runPower = limbSwingAmount * 1.0F * runWeight;

//            neck.xRot += runPower * 0.2F;
            head.xRot -= runPower * 0.2F;
            head.z += runPower * 0.5F;

            // bounding
            float bodyYOffset = AnimUtil.wave(runTime + 0.1F) * runPower;
            upperbody.y += bodyYOffset;
            upperbody.z += AnimUtil.wave(runTime - 0.25F + 0.1F) * 2;
            lowerbody.y += bodyYOffset * 0.25F;

            left_leg.y -= bodyYOffset;
            right_leg.y -= bodyYOffset;
            left_leg_back.y -= bodyYOffset * 1.5F;
            right_leg_back.y -= bodyYOffset * 1.5F;

            left_leg.xRot  += AnimUtil.wave(runTime + 0.2F)  * runPower * -0.8F;
            right_leg.xRot += AnimUtil.wave(runTime + 0.3F) * runPower * -0.8F;
            left_leg.z += AnimUtil.wave(runTime - 0.25F + 0.2F) * 1;
            right_leg.z += AnimUtil.wave(runTime - 0.25F + 0.2F) * 1;

            left_leg_back.xRot  += (AnimUtil.wave(runTime + 0.7F) - 0.25F) * runPower * -0.5F;
            right_leg_back.xRot += (AnimUtil.wave(runTime + 0.5F) - 0.25F) * runPower * -0.5F;
            left_leg_back.z += AnimUtil.wave(runTime - 0.25F + 0.2F) * 1;
            right_leg_back.z += AnimUtil.wave(runTime - 0.25F + 0.2F) * 1;

//            neck.y += AnimUtil.wave(runTime) * runPower * 0.5F;
//            neck.xRot += (AnimUtil.wave(runTime)/2.0F+0.5F) * runPower * 0.05F;

            head.y += AnimUtil.wave(runTime - 0.3F) * runPower * 0.5F;
//            neck.xRot += (AnimUtil.wave(runTime - 0.3F)/2.0F+0.5F) * runPower * 0.05F;

            float earXRot = (AnimUtil.wave(runTime)/2.0F+0.5F) * runPower * 0.2F;
            left_ear.xRot  += earXRot;
            right_ear.xRot += earXRot;
        }
    }
}