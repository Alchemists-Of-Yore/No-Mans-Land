package com.farcr.nomansland.common.mixin.integration;

import com.farcr.nomansland.common.block.FrostedGrassBlock;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sereneseasons.init.ModTags;
import sereneseasons.season.RandomUpdateHandler;
import sereneseasons.season.SeasonHooks;

@IfModLoaded("sereneseasons")
@Mixin(RandomUpdateHandler.class)
public class RandomUpdateHandlerMixin {

    @Inject(method = "meltInChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;", ordinal = 1, shift = At.Shift.AFTER))
    private static void meltFrostedGrass(ChunkMap chunkMap, LevelChunk chunkIn, float meltChance, CallbackInfo ci, @Local ServerLevel world, @Local Holder<Biome> biome, @Local(ordinal = 0) BlockPos topAirPos, @Local(ordinal = 1) BlockPos topGroundPos, @Local(ordinal = 0) BlockState aboveGroundState) {
        if (!biome.is(ModTags.Biomes.BLACKLISTED_BIOMES) && SeasonHooks.getBiomeTemperature(world, biome, topGroundPos) >= 0.15F) {
            if (aboveGroundState.is(NMLBlocks.FROSTED_GRASS)) {
                world.setBlockAndUpdate(topAirPos, aboveGroundState.getValue(FrostedGrassBlock.SNOWLOGGED) ? aboveGroundState.setValue(FrostedGrassBlock.SNOWLOGGED, false) : Blocks.SHORT_GRASS.defaultBlockState());
            }
        }
    }
}
