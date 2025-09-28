package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.moulberry.mixinconstraints.annotations.IfModAbsent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.SnowAndFreezeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.farcr.nomansland.common.block.FrostedGrassBlock.SNOWLOGGED;

@IfModAbsent("snowrealmagic")
@Mixin(SnowAndFreezeFeature.class)
public class SnowAndFreezeFeatureMixin {

    @Inject(method = "place", at = @At(value = "HEAD"), cancellable = true)
    private void place(FeaturePlaceContext<NoneFeatureConfiguration> p_160368_, CallbackInfoReturnable<Boolean> cir) {
        WorldGenLevel worldgenlevel = p_160368_.level();
        BlockPos blockpos = p_160368_.origin();
        BlockPos.MutableBlockPos mutableblockpos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableblockpos1 = new BlockPos.MutableBlockPos();

        for(int i = 0; i < 16; ++i) {
            for(int j = 0; j < 16; ++j) {
                int k = blockpos.getX() + i;
                int l = blockpos.getZ() + j;
                int i1 = worldgenlevel.getHeight(Heightmap.Types.MOTION_BLOCKING, k, l);
                mutableblockpos.set(k, i1, l);
                mutableblockpos1.set(mutableblockpos).move(Direction.DOWN, 1);
                Biome biome = worldgenlevel.getBiome(mutableblockpos).value();
                if (biome.shouldFreeze(worldgenlevel, mutableblockpos1, false)) {
                    worldgenlevel.setBlock(mutableblockpos1, Blocks.ICE.defaultBlockState(), 2);
                }

                if (biome.shouldSnow(worldgenlevel, mutableblockpos)) {
                    if (worldgenlevel.getBlockState(mutableblockpos).is(NMLBlocks.FROSTED_GRASS.block())) {
                        worldgenlevel.setBlock(mutableblockpos, NMLBlocks.FROSTED_GRASS.get().defaultBlockState().setValue(SNOWLOGGED, true), 2);
                    } else {
                        worldgenlevel.setBlock(mutableblockpos, Blocks.SNOW.defaultBlockState(), 2);
                    }
                    BlockState blockstate = worldgenlevel.getBlockState(mutableblockpos1);
                    if (blockstate.hasProperty(SnowyDirtBlock.SNOWY)) {
                        worldgenlevel.setBlock(mutableblockpos1, blockstate.setValue(SnowyDirtBlock.SNOWY, true), 2);
                    }
                }
            }
        }

        cir.setReturnValue(true);
    }
}
