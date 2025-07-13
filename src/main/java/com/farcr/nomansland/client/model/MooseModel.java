package com.farcr.nomansland.client.model;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.entity.Moose;
import net.minecraft.client.model.AgeableHierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.entity.animation.json.AnimationHolder;

public class MooseModel extends AgeableHierarchicalModel<Moose> {
    public static final AnimationHolder STOMP = getAnimation(NoMansLand.location("moose/stomp"));

    private final ModelPart root;
    private final ModelPart moose;
    private final ModelPart head;
    private final ModelPart upperbody;
    private final ModelPart lowerbody;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public MooseModel(ModelPart root) {
        super(0.5F, 24.0F);
        this.root = root;
        this.moose = root.getChild("moose");
        this.head = moose.getChild("head");
        this.upperbody = moose.getChild("upperbody");
        this.lowerbody = upperbody.getChild("lowerbody");
        this.rightHindLeg = lowerbody.getChild("rightHindLeg");
        this.leftHindLeg = lowerbody.getChild("leftHindLeg");
        this.rightFrontLeg = upperbody.getChild("rightFrontLeg");
        this.leftFrontLeg = upperbody.getChild("leftFrontLeg");
    }


    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition moose = partdefinition.addOrReplaceChild("moose", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, -10.0F));

        PartDefinition head = moose.addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 65).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(50, 0).addBox(-3.0F, -3.0F, -13.0F, 6.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

        PartDefinition rightear_r1 = head.addOrReplaceChild("rightear_r1", CubeListBuilder.create().texOffs(10, 41).mirror().addBox(-2.1014F, -9.2713F, -18.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.0F, 10.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition leftear_r1 = head.addOrReplaceChild("leftear_r1", CubeListBuilder.create().texOffs(10, 41).addBox(0.1014F, -9.2713F, -18.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 10.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition rightantler_r1 = head.addOrReplaceChild("rightantler_r1", CubeListBuilder.create().texOffs(36, 19).mirror().addBox(3.1894F, -23.2791F, -17.5951F, 8.0F, 8.0F, 14.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.0F, 10.0F, 0.6151F, -0.0497F, -1.2677F));

        PartDefinition rightantlerconnection_r1 = head.addOrReplaceChild("rightantlerconnection_r1", CubeListBuilder.create().texOffs(33, 41).mirror().addBox(-17.9067F, -2.5333F, -15.929F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.0F, 10.0F, -0.131F, -0.6387F, 0.4171F));

        PartDefinition snout_r1 = head.addOrReplaceChild("snout_r1", CubeListBuilder.create().texOffs(0, 65).addBox(-2.0F, -0.0342F, -7.5221F, 4.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, -13.0F, 0.4363F, 0.0F, 0.0F));

        PartDefinition leftantler_r1 = head.addOrReplaceChild("leftantler_r1", CubeListBuilder.create().texOffs(36, 19).addBox(-11.1894F, -23.2791F, -17.5951F, 8.0F, 8.0F, 14.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 2.0F, 10.0F, 0.6151F, 0.0497F, 1.2677F));

        PartDefinition leftantlerconnection_r1 = head.addOrReplaceChild("leftantlerconnection_r1", CubeListBuilder.create().texOffs(33, 41).addBox(13.9067F, -2.5333F, -15.929F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, 2.0F, 10.0F, -0.131F, 0.6387F, -0.4171F));

        PartDefinition dewlap = head.addOrReplaceChild("dewlap", CubeListBuilder.create().texOffs(0, 25).addBox(0.0F, 0.0F, -6.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(48, 66).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, -6.0F));

        PartDefinition upperbody = moose.addOrReplaceChild("upperbody", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, 0.0F, 0.0F, 10.0F, 18.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition lowerbody = upperbody.addOrReplaceChild("lowerbody", CubeListBuilder.create().texOffs(0, 33).addBox(-4.0F, -15.0F, 0.0F, 8.0F, 15.0F, 17.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 18.0F, 15.0F));

        PartDefinition tail_r1 = lowerbody.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(0, 41).addBox(-1.0F, -16.5445F, 14.6003F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.0F, -5.0F, -0.829F, 0.0F, 0.0F));

        PartDefinition rightHindLeg = lowerbody.addOrReplaceChild("rightHindLeg", CubeListBuilder.create().texOffs(64, 60).addBox(-1.0F, -3.0F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, 11.0F, -1.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0F, -4.0F, 11.0F));

        PartDefinition leftHindLeg = lowerbody.addOrReplaceChild("leftHindLeg", CubeListBuilder.create().texOffs(64, 60).mirror().addBox(-2.0F, -3.0F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 0).mirror().addBox(-2.0F, 11.0F, -1.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.0F, -4.0F, 11.0F));

        PartDefinition rightFrontLeg = upperbody.addOrReplaceChild("rightFrontLeg", CubeListBuilder.create().texOffs(50, 41).mirror().addBox(-1.0F, -6.0F, -3.0F, 4.0F, 19.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(35, 0).addBox(0.0F, 13.0F, -2.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 12.0F, 5.0F));

        PartDefinition leftFrontLeg = upperbody.addOrReplaceChild("leftFrontLeg", CubeListBuilder.create().texOffs(50, 41).addBox(-3.0F, -6.0F, -3.0F, 4.0F, 19.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(35, 0).mirror().addBox(-3.0F, 13.0F, -2.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.0F, 12.0F, 5.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public ModelPart root() {return this.root;}

    @Override
    public void setupAnim(Moose entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
        this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        this.animate(entity.stompAnimationState, STOMP, ageInTicks);
    }
}