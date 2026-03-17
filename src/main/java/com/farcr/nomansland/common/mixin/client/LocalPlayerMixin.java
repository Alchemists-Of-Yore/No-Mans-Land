package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.friend.dream.DreamManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @ModifyExpressionValue(
        method = "aiStep",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSleeping()Z")
    )
    private boolean modifyExpression(boolean original) {
        if (DreamManager.Client.getInstance().dreamShouldRender())
            return false;
        return original;
    }
}
