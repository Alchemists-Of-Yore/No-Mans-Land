package com.farcr.nomansland.common.mixin.client;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// todo: inverted bell seamless teleport black magic
@Mixin(targets = "net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public class ClientChunkCacheStorageMixin {
    @Shadow @Final ClientChunkCache this$0;

    /*
    @Inject(method = "inRange", at = @At("RETURN"), cancellable = true)
    private void allowDistantOverride(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            if (((ClientChunkCacheExtension)this.this$0).nml$isInOverride(x, z)) {
                cir.setReturnValue(true);
            }
        }
    }
    */
}
