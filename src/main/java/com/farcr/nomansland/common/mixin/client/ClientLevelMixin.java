package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.client.renderer.UpperAtmosphericRenderer;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
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

    @Inject(
            method = "getCloudColor",
            at = @At("RETURN"),
            cancellable = true
    )
    public void nml$friendMoonDarkensClouds(float partialTick, CallbackInfoReturnable<Vec3> cir) {
        Vec3 currentCloudColor = cir.getReturnValue();
        float darknessStrength = FriendMoonRenderer.getInstance().getFriendMoonDarkeningStrength();
        // same computation as LightTexture's darkness effect
        if (darknessStrength > 0.001F) cir.setReturnValue(currentCloudColor.add(-darknessStrength, -darknessStrength, -darknessStrength));
    }
}
