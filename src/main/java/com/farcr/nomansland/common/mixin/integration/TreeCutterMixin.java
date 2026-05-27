package com.farcr.nomansland.common.mixin.integration;

import com.farcr.nomansland.common.mixin.plugin.annotation.IfModPresent;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.simibubi.create.content.kinetics.saw.TreeCutter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@IfModPresent("create")
@Mixin(TreeCutter.class)
public class TreeCutterMixin {

    @Inject(method = "nonDecayingLeafDistance", at = @At("RETURN"), cancellable = true)
    private static void nonDecayingLeafDistance(BlockState state, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() == -1) {
            if (state.is(NMLBlocks.FIELD_MUSHROOM_BLOCK.block()) || state.is(NMLBlocks.SHELF_MUSHROOM_BLOCK.block())) cir.setReturnValue(2);
        }
    }
}
