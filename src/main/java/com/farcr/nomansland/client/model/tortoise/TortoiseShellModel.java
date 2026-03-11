package com.farcr.nomansland.client.model.tortoise;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class TortoiseShellModel extends EntityModel<LivingEntity> {
    public final ModelPart tortoiseShell;

    public TortoiseShellModel(ModelPart root) {
        tortoiseShell = root.getChild("tortoise_shell");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition tortoise_shell = partdefinition.addOrReplaceChild("tortoise_shell", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -25.0F, 0.0F, 16.0F, 18.0F, 6.0F, new CubeDeformation(0.5F))
                .texOffs(0, 24).addBox(-8.0F, -25.0F, -1.0F, 16.0F, 18.0F, 7.0F, new CubeDeformation(0.75F))
                .texOffs(42, 49).addBox(-4.0F, -24.0F, -2.0F, 8.0F, 12.0F, 3.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        tortoiseShell.resetPose();
        if (entity.isCrouching()) {
            tortoiseShell.xRot = 0.5F;
            tortoiseShell.z = 10.5F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        tortoiseShell.render(poseStack, buffer, packedLight, packedOverlay);
    }
}
