package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Spider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Spider.class)
public class SpiderMixin {
    @Inject(method =  "registerGoals", at = @At("TAIL"))
    private void addSpiderGoals(CallbackInfo ci) {
        ((Spider) (Object) this).goalSelector.addGoal(3, new AvoidEntityGoal<>((Spider) (Object) this, Moose.class, 6.0f, 1.0, 1.2));
    }
}
