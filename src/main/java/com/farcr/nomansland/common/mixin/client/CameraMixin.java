package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.dreams.DreamManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @ModifyExpressionValue(method = "setup",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"
        )
    )
    private static boolean nml$redirectSleeping(boolean original) {
        if (DreamManager.Client.getInstance().dreamShouldRender())
            return false;
        return original;
    }
}
