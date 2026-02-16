package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.surfacerule.NMLSteepMaterialCondition;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SurfaceRules.Context.SteepMaterialCondition.class)
public abstract class SteepMaterialConditionMixin extends SurfaceRules.LazyXZCondition {

    protected SteepMaterialConditionMixin(SurfaceRules.Context p_189622_) {
        super(p_189622_);
    }

    @Inject(method = "compute", at = @At(value = "HEAD"), cancellable = true)
    private void fixMountainBug(CallbackInfoReturnable<Boolean> cir)  {
        cir.setReturnValue(NMLSteepMaterialCondition.evaluate(context));

//        int lastZ = Math.max(chunkZ - 1, 0), nextZ = Math.min(chunkZ + 1, 15);
//        int lastZHeight = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, chunkX, lastZ);
//        int nextZHeight = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, chunkX, nextZ);
//        if (lastZHeight >= nextZHeight + 4) return true;
//
//        int lastX = Math.max(chunkX - 1, 0), nextX = Math.min(chunkX + 1, 15);
//        int lastXHeight = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, lastX, chunkZ);
//        int nextXHeight = chunkaccess.getHeight(Heightmap.Types.WORLD_SURFACE_WG, nextX, chunkZ);
//        if (nextXHeight >= lastXHeight + 4) return true;
//
//        return original;
    }
}
