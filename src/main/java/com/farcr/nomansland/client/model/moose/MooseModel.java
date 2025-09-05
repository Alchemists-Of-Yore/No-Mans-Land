package com.farcr.nomansland.client.model.moose;

import com.farcr.nomansland.client.model.utils.AnimUtil;
import com.farcr.nomansland.common.entity.moose.Moose;
import net.minecraft.client.model.AgeableHierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class MooseModel<T extends Moose> extends AgeableHierarchicalModel<T> {
    private final ModelPart root;
    private final ModelPart upper_body;
    private final ModelPart lower_body;
    private final ModelPart right_back_leg;
    private final ModelPart left_back_leg;
    private final ModelPart tail;
    private final ModelPart right_front_leg;
    private final ModelPart left_front_leg;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart dewlap;
    private final ModelPart left_ear;
    private final ModelPart right_ear;

    public MooseModel(ModelPart root) {
        super(0.5F, 24.0F);
        this.root = root;
        this.upper_body = root.getChild("upper_body");
        this.lower_body = this.upper_body.getChild("lower_body");
        this.right_back_leg = this.lower_body.getChild("right_back_leg");
        this.left_back_leg = this.lower_body.getChild("left_back_leg");
        this.tail = this.lower_body.getChild("tail");
        this.right_front_leg = this.upper_body.getChild("right_front_leg");
        this.left_front_leg = this.upper_body.getChild("left_front_leg");
        this.neck = this.upper_body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.dewlap = this.head.getChild("dewlap");
        this.left_ear = this.head.getChild("left_ear");
        this.right_ear = this.head.getChild("right_ear");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition upper_body = partdefinition.addOrReplaceChild("upper_body", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, 0.0F, 0.0F, 14.0F, 18.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, -16.0F));

        PartDefinition lower_body = upper_body.addOrReplaceChild("lower_body", CubeListBuilder.create().texOffs(0, 33).addBox(-6.0F, -15.0F, 0.0F, 12.0F, 15.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 18.0F, 15.0F));

        PartDefinition right_back_leg = lower_body.addOrReplaceChild("right_back_leg", CubeListBuilder.create().texOffs(70, 60).addBox(-1.0F, -3.0F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, 11.0F, -1.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -4.0F, 11.0F));

        PartDefinition left_back_leg = lower_body.addOrReplaceChild("left_back_leg", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.0F, 11.0F, -1.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(70, 60).mirror().addBox(-2.0F, -3.0F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(6.0F, -4.0F, 11.0F));

        PartDefinition tail = lower_body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -13.0F, 17.0F));

        PartDefinition tail_r1 = tail.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(0, 41).addBox(-1.0F, -16.5445F, 14.6003F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -23.0F, -0.829F, 0.0F, 0.0F));

        PartDefinition right_front_leg = upper_body.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(56, 41).mirror().addBox(-1.0F, -6.0F, -3.0F, 4.0F, 19.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(43, 0).addBox(0.0F, 13.0F, -2.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.0F, 12.0F, 5.0F));

        PartDefinition left_front_leg = upper_body.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(56, 41).addBox(-3.0F, -6.0F, -3.0F, 4.0F, 19.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(43, 0).mirror().addBox(-3.0F, 13.0F, -2.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(7.0F, 12.0F, 5.0F));

        PartDefinition neck = upper_body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(26, 64).addBox(-3.5F, -2.0F, -4.0F, 7.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(58, 0).addBox(-3.5F, -3.0F, -9.0F, 7.0F, 8.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -4.0F));

        PartDefinition leftantlerconnection_r1 = head.addOrReplaceChild("leftantlerconnection_r1", CubeListBuilder.create().texOffs(40, 41).addBox(13.9067F, -2.5333F, -15.929F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 2.0F, 14.0F, -0.131F, 0.6387F, -0.4171F));

        PartDefinition leftantler_r1 = head.addOrReplaceChild("leftantler_r1", CubeListBuilder.create().texOffs(44, 19).addBox(-11.1894F, -23.2791F, -17.5951F, 8.0F, 8.0F, 14.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 2.0F, 14.0F, 0.6151F, 0.0497F, 1.2677F));

        PartDefinition rightantler_r1 = head.addOrReplaceChild("rightantler_r1", CubeListBuilder.create().texOffs(44, 19).mirror().addBox(3.1894F, -23.2791F, -17.5951F, 8.0F, 8.0F, 14.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.0F, 14.0F, 0.6151F, -0.0497F, -1.2677F));

        PartDefinition rightantlerconnection_r1 = head.addOrReplaceChild("rightantlerconnection_r1", CubeListBuilder.create().texOffs(40, 41).mirror().addBox(-17.9067F, -2.5333F, -15.929F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.0F, 14.0F, -0.131F, -0.6387F, 0.4171F));

        PartDefinition snout_r1 = head.addOrReplaceChild("snout_r1", CubeListBuilder.create().texOffs(0, 64).addBox(-2.0F, -0.0342F, -7.5221F, 5.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -2.0F, -9.0F, 0.4363F, 0.0F, 0.0F));

        PartDefinition dewlap = head.addOrReplaceChild("dewlap", CubeListBuilder.create().texOffs(0, 25).addBox(0.0F, 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(52, 66).addBox(-1.0F, 0.0F, 5.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, -8.0F));

        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(2.0F, -3.0F, -3.0F));

        PartDefinition leftear_r1 = left_ear.addOrReplaceChild("leftear_r1", CubeListBuilder.create().texOffs(10, 41).addBox(0.1014F, -9.2713F, -18.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 5.0F, 17.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(-2.0F, -3.0F, -3.0F));

        PartDefinition rightear_r1 = right_ear.addOrReplaceChild("rightear_r1", CubeListBuilder.create().texOffs(10, 41).mirror().addBox(-2.1014F, -9.2713F, -18.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(2.0F, 5.0F, 17.0F, 0.0F, 0.0F, -0.1745F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
    
    @Override
    public void setupAnim(Moose entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

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

            neck.y += AnimUtil.wave(idleTime) * idlePower * 0.1F;
            head.y += AnimUtil.wave(idleTime - 0.2F) * idlePower * 0.05F;
            float bodyYOffset = AnimUtil.wave(idleTime + 0.2F) * idlePower * 0.1F;
            upper_body.y += bodyYOffset;
            left_front_leg.y -= bodyYOffset;
            right_front_leg.y -= bodyYOffset;
            left_back_leg.y -= bodyYOffset;
            right_back_leg.y -= bodyYOffset;
        }
        // LOOK ANIMATION
        {
            this.head.xRot += headPitch * 0.6F * Mth.DEG_TO_RAD;
            this.head.yRot += netHeadYaw * 0.6F * Mth.DEG_TO_RAD;
            this.neck.xRot += headPitch * 0.4F * Mth.DEG_TO_RAD;
            this.neck.yRot += netHeadYaw * 0.4F * Mth.DEG_TO_RAD;

//            float upperBodyXRot = headPitch * 0.1F * Mth.DEG_TO_RAD;
//            float upperBodyYRot = netHeadYaw * 0.05F * Mth.DEG_TO_RAD;
//            this.upper_body.xRot += upperBodyXRot;
//            this.upper_body.yRot += upperBodyYRot;
//            left_front_leg.xRot -= upperBodyXRot;
//            right_front_leg.xRot -= upperBodyXRot;
//            left_back_leg.xRot -= upperBodyXRot;
//            right_back_leg.xRot -= upperBodyXRot;
//            left_front_leg.yRot -= upperBodyYRot;
//            right_front_leg.yRot -= upperBodyYRot;
//            left_back_leg.yRot -= upperBodyYRot;
//            right_back_leg.yRot -= upperBodyYRot;

            this.dewlap.xRot -= Math.min(headPitch * Mth.DEG_TO_RAD, 0.1F);
        }

        // LOCOMOTION
        float runWeight = Math.clamp((limbSwingAmount - 0.6F) * 7, 0, 1);
        float walkWeight = 1 - runWeight;

        // WALK ANIMATION
        {
            float walkTime = limbSwing * 0.2F;
            float walkPower = Math.min(limbSwingAmount * 3.5F, 0.8F) * walkWeight;
            float bodyYOffset =  AnimUtil.wave(walkTime * 2) * walkPower * -0.3F;
            upper_body.y += bodyYOffset;
            upper_body.z += AnimUtil.wave(walkTime * 2 + 0.1F) * walkPower * 0.3F;

            left_front_leg.y -= bodyYOffset;
            right_front_leg.y -= bodyYOffset;
            left_back_leg.y -= bodyYOffset;
            right_back_leg.y -= bodyYOffset;

            left_front_leg.xRot += AnimUtil.wave(walkTime + 0.5F - 0.1F) * walkPower * 0.2F;
            left_front_leg.y -= Math.max(AnimUtil.wave(walkTime), 0) * walkPower * 4;
            left_front_leg.z -= AnimUtil.wave(walkTime + 0.75F) * walkPower * 3;

            right_front_leg.xRot += AnimUtil.wave(walkTime - 0.1F) * walkPower * 0.2F;
            right_front_leg.y -= Math.max(AnimUtil.wave(walkTime + 0.5F), 0) * walkPower * 4;
            right_front_leg.z -= AnimUtil.wave(walkTime + 0.25F) * walkPower * 3;

            walkTime += 0.1F;
            left_back_leg.xRot += AnimUtil.wave(walkTime - 0.1F) * walkPower * 0.1F;
            left_back_leg.y -= Math.max(AnimUtil.wave(walkTime + 0.5F), 0) * walkPower * 2;
            left_back_leg.z -= AnimUtil.wave(walkTime + 0.25F) * walkPower * 3;
            walkTime -= 0.1F;

            right_back_leg.xRot += AnimUtil.wave(walkTime + 0.5F - 0.1F) * walkPower * 0.1F;
            right_back_leg.y -= Math.max(AnimUtil.wave(walkTime), 0) * walkPower * 2;
            right_back_leg.z -= AnimUtil.wave(walkTime + 0.75F) * walkPower * 3;

            neck.y += AnimUtil.wave(walkTime * 2 - 0.2F) * walkPower * -0.2F;
            neck.xRot += AnimUtil.wave(walkTime * 2) * walkPower * -0.005F;
            head.y += AnimUtil.wave(walkTime * 2 - 0.4F) * walkPower * -0.1F;

            tail.y += AnimUtil.wave(walkTime * 2 + 0.2F) * walkPower * -0.1F;

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

            neck.xRot += runPower * 0.2F;
            head.xRot -= runPower * 0.2F;
            head.z += runPower * 0.5F;

            // bounding
            float bodyYOffset = AnimUtil.wave(runTime + 0.1F) * runPower;
            upper_body.y += bodyYOffset;
            upper_body.z += AnimUtil.wave(runTime - 0.25F + 0.1F) * 2;
            lower_body.y += bodyYOffset * 0.25F;

            left_front_leg.y -= bodyYOffset;
            right_front_leg.y -= bodyYOffset;
            left_back_leg.y -= bodyYOffset * 1.5F;
            right_back_leg.y -= bodyYOffset * 1.5F;

            left_front_leg.xRot  += AnimUtil.wave(runTime + 0.2F)  * runPower * -0.8F;
            right_front_leg.xRot += AnimUtil.wave(runTime + 0.3F) * runPower * -0.8F;
            left_front_leg.z += AnimUtil.wave(runTime - 0.25F + 0.2F) * 1;
            right_front_leg.z += AnimUtil.wave(runTime - 0.25F + 0.2F) * 1;

            left_back_leg.xRot  += (AnimUtil.wave(runTime + 0.7F) - 0.25F) * runPower * -0.5F;
            right_back_leg.xRot += (AnimUtil.wave(runTime + 0.5F) - 0.25F) * runPower * -0.5F;
            left_back_leg.z += AnimUtil.wave(runTime - 0.25F + 0.2F) * 1;
            right_back_leg.z += AnimUtil.wave(runTime - 0.25F + 0.2F) * 1;

            neck.y += AnimUtil.wave(runTime) * runPower * 0.5F;
            neck.xRot += (AnimUtil.wave(runTime)/2.0F+0.5F) * runPower * 0.05F;

            head.y += AnimUtil.wave(runTime - 0.3F) * runPower * 0.5F;
            neck.xRot += (AnimUtil.wave(runTime - 0.3F)/2.0F+0.5F) * runPower * 0.05F;

            float earXRot = (AnimUtil.wave(runTime)/2.0F+0.5F) * runPower * 0.2F;
            left_ear.xRot  += earXRot;
            right_ear.xRot += earXRot;
        }
    }
}