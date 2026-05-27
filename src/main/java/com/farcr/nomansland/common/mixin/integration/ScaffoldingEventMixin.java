package com.farcr.nomansland.common.mixin.integration;

import com.farcr.nomansland.common.mixin.plugin.annotation.IfModPresent;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.natamus.scaffoldingdropsnearby_common_neoforge.events.ScaffoldingEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@IfModPresent("scaffoldingdropsnearby")
@Mixin(ScaffoldingEvent.class)
public class ScaffoldingEventMixin {

    @Shadow @Final private static CopyOnWriteArrayList<BlockPos> lastScaffoldings;
    @Shadow @Final private static HashMap<BlockPos, Date> lastaction;

    @Inject(method = "onBlockBreak", at = @At("TAIL"))
    private static void acceptWoodenScaffolding(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, CallbackInfo ci) {
        if (!world.isClientSide) {
            if (state.is(NMLBlocks.WOODEN_SCAFFOLDING)) {
                lastScaffoldings.add(pos);
                lastaction.put(pos, new Date());
            }
        }
    }
}
