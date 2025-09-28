package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.farcr.nomansland.common.block.FrostedGrassBlock.SNOWLOGGED;

@IfModAbsent("sereneseasons")
@Mixin(Biome.class)
public class BiomeMixin {

    @Inject(method = "shouldSnow", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", shift = At.Shift.AFTER), cancellable = true)
    private void snowOnShortGrass(LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(!NMLConfig.GRASS_FROSTING.get()) return;
        if(Mods.SNOWREALMAGIC.isLoaded()) return; // We can't use a second IfModAbsent because mixinconstraints 1.0.7 is bugged and other dependencies are incompatible with the newer version.

        BlockState blockstate = level.getBlockState(pos);
        if (blockstate.is(Blocks.SHORT_GRASS) || blockstate.is(NMLBlocks.FROSTED_GRASS.get()) && !blockstate.getValue(SNOWLOGGED)) {
            cir.setReturnValue(true);
        }
    }
}
