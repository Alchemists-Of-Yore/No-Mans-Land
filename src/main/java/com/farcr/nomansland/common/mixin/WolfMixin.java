package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.entity.cervidae.deer.Deer;
import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.animal.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Wolf.class)
public class WolfMixin {

    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addWolfGoals(CallbackInfo ci) {
        // Target deer
        ((Wolf) (Object) this).goalSelector.addGoal(5, new NonTameRandomTargetGoal<>(((Wolf) (Object) this), Deer.class, false, target -> true));

        // Avoid moose
        // This SHOULD use the WolfAvoidEntityGoal but for some reason that's not working. Someone who isn't stupid will have to debug at some point
        //((Wolf) (Object) this).goalSelector.addGoal(3, ((Wolf)(Object)this).new WolfAvoidEntityGoal<>(((Wolf)(Object) this), Moose.class, 24.0f, (double) 1.5F, (double) 1.5f));
        ((Wolf) (Object) this).goalSelector.addGoal(3, new AvoidEntityGoal<>(((Wolf)(Object) this), Moose.class, 24.0f, (double) 1.5F, (double) 1.5f));
    }
}
