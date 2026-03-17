package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.friend.dream.DreamManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(
        method = "renderLevel",
        at = @At("HEAD")
    )
    private void nml$renderLevelHead(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (DreamManager.Client.getInstance().dreamShouldRender())
            Minecraft.getInstance().level = DreamManager.Client.getInstance().getLevelHandler().getFakeLevel();
    }

    @Inject(
        method = "renderLevel",
        at = @At("TAIL")
    )
    private void nml$renderLevelTail(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (DreamManager.Client.getInstance().dreamShouldRender())
            Minecraft.getInstance().level = DreamManager.Client.getInstance().getLevelHandler().getOriginalLevel();
    }
}
