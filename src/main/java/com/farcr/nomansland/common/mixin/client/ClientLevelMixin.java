package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.UpperAtmosphericRenderer;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
    @Inject(
            method = "getStarBrightness",
            at = @At("RETURN"),
            cancellable = true
    )
    public void nml$getStarBrightness(float partialTick, CallbackInfoReturnable<Float> cir) {
        float starBrightness = cir.getReturnValue();
        ClientDreamRenderer manager = ClientDreamRenderer.getInstance();
        if (manager.dreamShouldRender() && manager.getRenderer() != null) {
            cir.setReturnValue(manager.getRenderer().getStarBrightness(partialTick, starBrightness));
            return;
        }
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        if (Minecraft.getInstance().level != null)
            starBrightness = Math.max(starBrightness, UpperAtmosphericRenderer.INSTANCE.getUpperAtmosphereFactor(camera.getPosition().y()));
        cir.setReturnValue(starBrightness);
    }
}
