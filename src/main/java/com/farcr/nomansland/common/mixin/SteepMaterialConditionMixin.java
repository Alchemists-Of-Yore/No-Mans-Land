package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.surfacerule.NMLSteepMaterialCondition;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SurfaceRules.Context.SteepMaterialCondition.class)
public abstract class SteepMaterialConditionMixin extends SurfaceRules.LazyXZCondition {

    protected SteepMaterialConditionMixin(SurfaceRules.Context p_189622_) {
        super(p_189622_);
    }

    @ModifyReturnValue(method = "compute", at = @At("RETURN"))
    private boolean fixMountainBug(boolean original) {
        return original || NMLSteepMaterialCondition.evaluate(context);
    }
}
