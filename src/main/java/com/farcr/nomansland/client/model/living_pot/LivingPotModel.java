package com.farcr.nomansland.client.model.living_pot;

import com.farcr.nomansland.common.entity.living_pot.LivingPot;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class LivingPotModel<T extends LivingPot> extends HierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart smallLeft;
    private final ModelPart smallRight;
    private final ModelPart largeLeft;
    private final ModelPart largeRight;

    public LivingPotModel(ModelPart root) {
        this.root = root;
        ModelPart bone = root.getChild("bone");
        this.body = bone.getChild("body");
        ModelPart legs = bone.getChild("legs");
        ModelPart left = legs.getChild("Left");
        ModelPart right = legs.getChild("Right");
        this.smallLeft = left.getChild("small");
        this.smallRight = right.getChild("small");
        this.largeLeft = left.getChild("large");
        this.largeRight = right.getChild("large");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0, 24, 0));

        bone.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0, 20F, 0));

        PartDefinition legs = bone.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(3, -5, 0));

        PartDefinition left = legs.addOrReplaceChild("Left", CubeListBuilder.create(), PartPose.offset(0, 0, 0));
        left.addOrReplaceChild("small", CubeListBuilder.create().texOffs(0, 9).addBox(-1, 0, -1, 2, 5, 2, new CubeDeformation(0)), PartPose.ZERO);
        left.addOrReplaceChild("large", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -1.0F, 7.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 0.0F, -8.0F));

        PartDefinition right = legs.addOrReplaceChild("Right", CubeListBuilder.create(), PartPose.offset(-6, 0, 0));
        right.addOrReplaceChild("small", CubeListBuilder.create().texOffs(0, 9).mirror().addBox(-1, 0, -1, 2, 5, 2, new CubeDeformation(0)).mirror(false), PartPose.ZERO);
        right.addOrReplaceChild("large", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-13.0F, -1.0F, 7.0F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(11.0F, 0.0F, -8.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public ModelPart getBody() {
        return body;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);

        smallLeft.visible = entity.isSmall();
        smallRight.visible = entity.isSmall();
        largeLeft.visible = entity.isLarge();
        largeRight.visible = entity.isLarge();

        boolean isDashing = entity.dashStartAnimState.isStarted() || entity.dashLoopAnimState.isStarted();
        if (!isDashing) {
            if (entity.isSmall()) {
                if (limbSwingAmount > 0.6F) {
                    animateWalk(LivingPotAnimation.RUN, limbSwing, limbSwingAmount, 2, 3);
                } else {
                    animateWalk(LivingPotAnimation.WALK, limbSwing, limbSwingAmount, 4, 6);
                }
            } else {
                animateWalk(LivingPotAnimation.WALK2, limbSwing, limbSwingAmount, 4, 5);
            }
        }

        animate(entity.dashStartAnimState, LivingPotAnimation.DASH_START, ageInTicks);
        animate(entity.dashLoopAnimState, LivingPotAnimation.DASH_LOOP, ageInTicks);
        animate(entity.dashEndAnimState, LivingPotAnimation.DASH_END, ageInTicks);
        animate(entity.attackAnimState, LivingPotAnimation.ATTACK_BLEND, ageInTicks);
        animate(entity.wakeUpAnimState, LivingPotAnimation.WAKE_UP, ageInTicks);
        animate(entity.sleepAnimState, LivingPotAnimation.SLEEP, ageInTicks);
    }
}
