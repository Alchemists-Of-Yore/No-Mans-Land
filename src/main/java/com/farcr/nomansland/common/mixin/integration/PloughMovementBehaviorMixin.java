package com.farcr.nomansland.common.mixin.integration;

import com.farcr.nomansland.common.mixin.plugin.annotation.IfModPresent;
import com.simibubi.create.content.contraptions.actors.plough.PloughMovementBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@IfModPresent("create")
@Mixin(PloughMovementBehaviour.class)
public class PloughMovementBehaviorMixin {

    @Inject(method = "canBreak", at = @At("RETURN"), cancellable = true)
    private void breakRails(Level world, BlockPos breakingPos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (state.getBlock() instanceof BaseRailBlock) cir.setReturnValue(true);
    }
}
