package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.common.extension.ClientChunkCacheExtension;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;

// todo: inverted bell seamless teleport black magic
@Mixin(ClientChunkCache.class)
public class ClientChunkCacheMixin implements ClientChunkCacheExtension {
    @Unique
    private HashMap<ChunkPos, Integer> nml$distOverrides = new HashMap<>();

    /*
    @Inject(method = "tick", at = @At("TAIL"))
    private void nml$tickOverrides(BooleanSupplier hasTimeLeft, boolean tickChunks, CallbackInfo ci) {
        Iterator<Map.Entry<ChunkPos, Integer>> it = this.nml$distOverrides.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ChunkPos, Integer> entry = it.next();
            int value = entry.getValue() - 1;
            if (value <= 0) {
                it.remove();
            } else {
                entry.setValue(value);
            }
        }
    }
    */

    @Override
    public void nml$addToOverride(int x, int z) {
//        this.nml$distOverrides.put(new ChunkPos(x, z), 100);
    }

    @Override
    public boolean nml$isInOverride(int x, int z) {
        return this.nml$distOverrides.containsKey(new ChunkPos(x, z));
    }
}
