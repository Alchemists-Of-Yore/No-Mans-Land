package com.farcr.nomansland.common.mixin.integration;

import com.farcr.nomansland.common.integration.FDIntegration;
import com.farcr.nomansland.common.mixin.plugin.annotation.IfModPresent;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.RichSoilBlock;

@IfModPresent("farmersdelight")
@Mixin(RichSoilBlock.class)
public class RichSoilBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void tryConvertFieldMushroom(BlockState state, ServerLevel level, BlockPos pos, RandomSource rand, CallbackInfo ci) {
        if (!level.isClientSide && level.getBlockState(pos.above()).getBlock() == NMLBlocks.FIELD_MUSHROOM.get()) {
            level.setBlockAndUpdate(pos.above(), (FDIntegration.FIELD_MUSHROOM_COLONY.get()).defaultBlockState());
            ci.cancel();
        }
    }
}
