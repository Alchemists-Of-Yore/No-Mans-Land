package com.farcr.nomansland.client.model.living_pot;

import com.farcr.nomansland.common.entity.living_pot.LivingPot;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Model for Living Pots (both small and large sizes).
 * Only renders legs — the pot body is rendered as a block model via LivingPotBodyLayer.
 * The "body" bone has no cubes but is kept for animation targeting.
 */
public class LivingPotModel<T extends LivingPot> extends HierarchicalModel<T> {

    private final ModelPart root;
    private final ModelPart bone;
    private final ModelPart body;
    private final ModelPart legs;
    private final ModelPart Left;
    private final ModelPart Right;

    public LivingPotModel(ModelPart root) {
        this.root = root;
        this.bone = root.getChild("bone");
        this.body = this.bone.getChild("body");
        this.legs = this.bone.getChild("legs");
        this.Left = this.legs.getChild("Left");
        this.Right = this.legs.getChild("Right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        // Body is an empty bone (no cubes) — the block model is rendered via LivingPotBodyLayer
        PartDefinition body = bone.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0, 0.0F));

        PartDefinition legs = bone.addOrReplaceChild("legs", CubeListBuilder.create(), PartPose.offset(3.0F, -5.0F, 0.0F));

        PartDefinition Left = legs.addOrReplaceChild("Left", CubeListBuilder.create().texOffs(0, 9).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Right = legs.addOrReplaceChild("Right", CubeListBuilder.create().texOffs(0, 9).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-6.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public ModelPart getBone() {
        return bone;
    }

    public ModelPart getBody() {
        return body;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);

        // Walk animations: skip during dash because DASH_START/LOOP include their own leg movement.
        // Layering walk on top of the dash animations would double-animate the legs.
        boolean isDashing = entity.dashStartAnimState.isStarted() || entity.dashLoopAnimState.isStarted();
        if (!isDashing) {
            if (entity.isSmall()) {
                if (limbSwingAmount > 0.6F) {
                    animateWalk(LivingPotAnimation.RUN, limbSwing, limbSwingAmount, 2, 3);
                } else {
                    animateWalk(LivingPotAnimation.WALK, limbSwing, limbSwingAmount, 4, 5);
                }
            } else {
                animateWalk(LivingPotAnimation.WALK2, limbSwing, limbSwingAmount, 4, 5);
            }
        }

        // State-driven animations (dash anims are self-contained run cycles)
        animate(entity.dashStartAnimState, LivingPotAnimation.DASH_START, ageInTicks);
        animate(entity.dashLoopAnimState, LivingPotAnimation.DASH_LOOP, ageInTicks);
        animate(entity.dashEndAnimState, LivingPotAnimation.DASH_END, ageInTicks);
        animate(entity.attackAnimState, LivingPotAnimation.ATTACK_BLEND, ageInTicks);
        animate(entity.wakeUpAnimState, LivingPotAnimation.WAKE_UP, ageInTicks);
        animate(entity.sleepAnimState, LivingPotAnimation.SLEEP, ageInTicks);
    }
}
