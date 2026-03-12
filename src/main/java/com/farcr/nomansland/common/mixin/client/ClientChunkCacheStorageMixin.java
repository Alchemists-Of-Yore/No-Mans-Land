package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.extension.ClientChunkCacheExtension;
import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public class ClientChunkCacheStorageMixin {
    @Shadow @Final ClientChunkCache this$0;

    @Inject(method = "inRange", at = @At("RETURN"), cancellable = true)
    private void allowDistantOverride(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            if (((ClientChunkCacheExtension)this.this$0).nml$isInOverride(x, z)) {
                cir.setReturnValue(true);
            }
        }
    }
}
