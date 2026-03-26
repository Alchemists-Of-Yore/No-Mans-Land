package com.farcr.nomansland.client.model;

import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.FastColor;

public class BuddyModel<T extends Buddy> extends PlayerModel<T> {

    public BuddyModel(ModelPart root) {
        super(root, true);
    }

    public float ascensionAlpha = 1.0f;

    @Override
    public void setupAnim(T buddy, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(buddy, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float tilt = buddy.getHeadTiltAmount();
        if (tilt != 0) {
            this.head.zRot = tilt;
            this.hat.zRot = tilt;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        if (ascensionAlpha < 1.0f) {
            int originalAlpha = FastColor.ARGB32.alpha(color);
            int newAlpha = (int) (originalAlpha * ascensionAlpha);
            color = FastColor.ARGB32.color(newAlpha, FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color));
        }
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = PlayerModel.createMesh(CubeDeformation.NONE, true);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Head = partdefinition.getChild("head");
        PartDefinition RightArm = partdefinition.getChild("right_arm");

        Head.addOrReplaceChild("Fungus_r1", CubeListBuilder.create().texOffs(50, 16).addBox(-3.0F, -4.0F, 0.0F, 6.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, -1.0F, 0.0F, -0.7854F, 0.0F));
        Head.addOrReplaceChild("Fungus_r2", CubeListBuilder.create().texOffs(50, 16).addBox(-3.0F, -4.0F, 0.0F, 6.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, -1.0F, 0.0F, 0.7854F, 0.0F));

        RightArm.addOrReplaceChild("Fungus_r3", CubeListBuilder.create().texOffs(54, 24).addBox(-4.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 5.0F, 0.0F, 0.0F, 0.3927F, 0.0F));
        RightArm.addOrReplaceChild("Fungus_r4", CubeListBuilder.create().texOffs(54, 20).addBox(-4.0F, -4.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 5.0F, 0.0F, 0.0F, -0.3927F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}
