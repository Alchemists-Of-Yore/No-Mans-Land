package com.farcr.nomansland.client.model.tortoise;

import com.farcr.nomansland.common.entity.tortoise.Tortoise;
import net.minecraft.client.model.AgeableHierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class TortoiseModel<T extends Tortoise> extends AgeableHierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart head;

    public TortoiseModel(ModelPart root) {
        super(1/6F, 120);
        this.root = root;
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 37)
                .addBox(-9, -15, -1, 18, 15, 22, new CubeDeformation(0.5F))
                .texOffs(0, 0)
                .addBox(-9, -15, -1, 18, 15, 22, new CubeDeformation(0)),
                PartPose.offset(0, 20, -10)
        );

        root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 37)
                .addBox(-9, -15, -1, 18, 15, 22, new CubeDeformation(0.5F))
                .texOffs(0, 0)
                .addBox(-9, -15, -1, 18, 15, 22, false),
                PartPose.offset(0, 20, -10)
        );

        root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(26, 74)
                .addBox(-2, -2, -6, 4, 4, 8, false)
                .texOffs(80, 0)
                .addBox(-2, -6, -6, 4, 4, 4, false)
                .texOffs(0, 74)
                .addBox(-3, -11, -9, 6, 5, 7, false),
                PartPose.offset(0, 17, -11)
        );

        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                        .texOffs(50, 74)
                        .addBox(-2.5F, -4, -2.5F, 5, 8, 5, true),
                PartPose.offsetAndRotation(-8.8536F, 20, -10.8536F, 0, 0.7854F, 0)
        );
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(50, 74)
                .addBox(-2.5F, -4, -2.5F, 5, 8, 5, false),
                PartPose.offsetAndRotation(8.8536F, 20, -10.8536F, 0, -0.7854F, 0)
        );
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(50, 74)
                .addBox(-2.5F, -4, -2.5F, 5, 8, 5, true),
                PartPose.offsetAndRotation(-8.8536F, 20, 10.8536F, 0, 0.7854F, 0)
        );
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(50, 74)
                .addBox(-2.5F, -4, -2.5F, 5, 8, 5, false),
                PartPose.offsetAndRotation(8.8536F, 20, 10.8536F, 0, -0.7854F, 0)
        );

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T tortoise, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        head.xRot = headPitch * (float) (Math.PI / 180.0);
        head.yRot = netHeadYaw * (float) (Math.PI / 180.0);

        animateWalk(TortoiseAnimation.TORTOISE_WALK, limbSwing, limbSwingAmount, 15, 200);
        animate(tortoise.emergingAnimationState, TortoiseAnimation.TORTOISE_EMERGE, ageInTicks);
        animate(tortoise.hidingAnimationState, TortoiseAnimation.TORTOISE_HIDE, ageInTicks);
        animate(tortoise.layingEggAnimationState, TortoiseAnimation.TORTOISE_LAY_EGG, ageInTicks);

        if (young) {
            head.xRot /= 2;
            head.yRot /= 2;
            applyStatic(TortoiseAnimation.BABY_TRANSFORM);
        }
    }

    @Override
    public ModelPart root() {
        return root;
    }
}