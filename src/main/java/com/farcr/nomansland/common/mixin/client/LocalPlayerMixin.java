package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.NMLMooseChargeAttackHandler;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.DreamType;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;jumpableVehicle()Lnet/minecraft/world/entity/PlayerRideableJumping;"))
    private PlayerRideableJumping nml$initiateMooseChargeAttack(LocalPlayer instance, Operation<PlayerRideableJumping> original, @Local(ordinal = 0) boolean wasJumping) {
        if (NMLMooseChargeAttackHandler.handleCustomChargeAttackLogic(instance, wasJumping)) {
            return null;
        }
        return original.call(instance);
    }

    @Inject(method = "startUsingItem", at = @At("HEAD"), cancellable = true)
    private void nml$cancelUseItem(InteractionHand hand, CallbackInfo ci) {
        if (ClientDreamRenderer.getInstance().dreamShouldRender()) ci.cancel();
    }

    @Inject(method = "drop", at = @At("HEAD"), cancellable = true)
    private void nml$cancelDropInDream(boolean fullStack, CallbackInfoReturnable<Boolean> cir) {
        if (ClientDreamRenderer.getInstance().dreamShouldRender()) cir.setReturnValue(false);
    }

    @Inject(method = "canStartSprinting", at = @At("RETURN"), cancellable = true)
    private void nml$cancelSprintInDream(CallbackInfoReturnable<Boolean> cir) {
        Player self = ((LocalPlayer) (Object) this);
        DreamType dreamType = DreamManager.getAmbiguousDreamType(self);
        if (dreamType != null && !dreamType.canSprint && !self.isCreative())
            cir.setReturnValue(false);
    }
}
