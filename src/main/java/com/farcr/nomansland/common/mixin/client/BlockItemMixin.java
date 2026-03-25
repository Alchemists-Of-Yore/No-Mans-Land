package com.farcr.nomansland.common.mixin.client;

import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    // word of the day: Impede
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void nml$impedePlacement(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (context.getPlayer().isLocalPlayer() && ClientDreamRenderer.getInstance().dreamShouldRender())
            cir.setReturnValue(InteractionResult.FAIL);
    }
}
