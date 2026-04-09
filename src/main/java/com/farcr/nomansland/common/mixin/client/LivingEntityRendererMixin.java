package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {

    @Shadow
    protected abstract float getBob(T livingBase, float partialTick);

    @Inject(
        method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V")
    )
    private void nml$buddyJump(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (entity.hasEffect(NMLEffects.HAPPINESS)) {
            float time = (getBob(entity, partialTicks) / Buddy.DIVIDE_TIME_CONSTANT);
            poseStack.translate(0, Buddy.getHappinessYDisplacement(time), 0);
        }
    }

    @Inject(method = "isShaking", at = @At("RETURN"), cancellable = true)
    private void NML$isShaking(T entity, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() || entity.NML$isBeingInspected());
    }
}
