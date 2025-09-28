package com.farcr.nomansland.common.mixin.integration;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sereneseasons.season.SeasonHooks;

import static com.farcr.nomansland.common.block.FrostedGrassBlock.SNOWLOGGED;

@IfModLoaded("sereneseasons")
@IfModAbsent("snowrealmagic")
@Mixin(SeasonHooks.class)
public class SeasonHooksMixin {

    @Inject(method = "shouldSnowHook", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;", shift = At.Shift.AFTER), cancellable = true)
    private static void snowOnShortGrass(Biome biome, LevelReader levelReader, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if(!NMLConfig.GRASS_FROSTING.get()) return;

        BlockState blockstate = levelReader.getBlockState(pos);
        if (blockstate.is(Blocks.SHORT_GRASS) || blockstate.is(NMLBlocks.FROSTED_GRASS.get()) && !blockstate.getValue(SNOWLOGGED)) {
            cir.setReturnValue(true);
        }
    }
}
