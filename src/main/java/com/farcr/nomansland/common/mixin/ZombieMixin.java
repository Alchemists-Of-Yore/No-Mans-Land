package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.entity.cervidae.moose.Moose;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public class ZombieMixin {
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void addZombieGoals(CallbackInfo ci) {
        ((Zombie) (Object) this).goalSelector.addGoal(3, new AvoidEntityGoal<>((Zombie) (Object) this, Moose.class, 6.0f, 1.0, 1.2));
    }
}
