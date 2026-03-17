package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.friend.dream.DreamManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
