package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.registry.items.NMLItems;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends LivingEntity> extends AgeableListModel<T> implements ArmedModel, HeadedModel {
    @Shadow
    @Final
    public ModelPart leftArm;

    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart rightLeg;

    @Shadow
    @Final
    public ModelPart leftLeg;

    @Shadow
    public HumanoidModel.ArmPose leftArmPose;

    @Shadow
    public HumanoidModel.ArmPose rightArmPose;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "TAIL"), cancellable = true)
    private void setupAnims(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity.getItemBySlot(EquipmentSlot.CHEST).is(NMLItems.TORTOISE_SHELL.get()) && this.attackTime <= 0) {
            boolean flag = entity.getFallFlyingTicks() > 4;
            float f = 1.0F;
            if (flag) {
                f = (float) entity.getDeltaMovement().lengthSqr();
                f /= 0.2F;
                f *= f * f;
            }

            if (f < 1.0F) {
                f = 1.0F;
            }
            float movementRight = (Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / f);
            float movementLeft = (Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f);
            boolean rightArmPosed = rightArmPose != HumanoidModel.ArmPose.EMPTY && rightArmPose != HumanoidModel.ArmPose.ITEM;
            boolean leftArmPosed = leftArmPose != HumanoidModel.ArmPose.EMPTY && leftArmPose != HumanoidModel.ArmPose.ITEM;
            if (!rightArmPosed && !leftArmPose.isTwoHanded()) {
                if (movementRight <= 0) {
                    rightArm.xRot = -Math.abs(movementRight);
                } else {
                    rightArm.xRot = 0.0F;
                }
            }
            if (!leftArmPosed && !rightArmPose.isTwoHanded()) {
                if (movementRight >= 0) { // So that the left arm does not perfectly coordinate with the right arm
                    leftArm.xRot = -Math.abs(movementLeft);
                } else {
                    leftArm.xRot = 0.0F;
                }
            }
        }
    }
}
