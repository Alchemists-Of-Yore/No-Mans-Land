package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.extension.EntityExtension;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin implements EntityExtension {

    @Inject(method = "isMergable", at = @At("RETURN"), cancellable = true)
    public void NML$avoidMerging(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() && !NML$isBeingInspected());
    }

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    public void NML$avoidPickup(Player entity, CallbackInfo ci) {
        if (NML$isBeingInspected())
            ci.cancel();
    }
}
