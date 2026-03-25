package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.NMLMooseChargeAttackHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

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
}
