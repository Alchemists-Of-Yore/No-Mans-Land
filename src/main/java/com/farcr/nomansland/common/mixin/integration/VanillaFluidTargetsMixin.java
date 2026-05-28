package com.farcr.nomansland.common.mixin.integration;

import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.mixin.plugin.annotation.IfModPresent;
import com.farcr.nomansland.common.registry.NMLFluids;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.simibubi.create.content.fluids.pipes.VanillaFluidTargets;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.farcr.nomansland.common.block.cauldrons.FourLayeredCauldronBlock.LEVEL;

@IfModPresent("create")
@Mixin(VanillaFluidTargets.class)
public class VanillaFluidTargetsMixin {

    @Inject(method = "canProvideFluidWithoutCapability", at = @At("RETURN"), cancellable = true)
    private static void canProvideFluidWithoutCapability(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            cir.setReturnValue(state.is(NMLBlocks.MILK_CAULDRON.block()) || state.is(NMLBlocks.HONEY_CAULDRON.block()) || state.is(NMLBlocks.RESIN_OIL_CAULDRON.block()));
        }
    }

    @Inject(method = "drainBlock", at = @At("RETURN"), cancellable = true)
    private static void drainBlock(Level level, BlockPos pos, BlockState state, boolean simulate, CallbackInfoReturnable<FluidStack> cir) {
        if (cir.getReturnValue() == FluidStack.EMPTY && (state.is(NMLBlocks.MILK_CAULDRON.block()) || state.is(NMLBlocks.HONEY_CAULDRON.block()) || state.is(NMLBlocks.RESIN_OIL_CAULDRON.block()))) {
            if (!simulate) level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());

            cir.setReturnValue(new FluidStack(state.is(NMLBlocks.RESIN_OIL_CAULDRON.block()) ? NMLFluids.RESIN_OIL.get() : state.is(NMLBlocks.HONEY_CAULDRON.block()) ? Mods.CREATE.getFluid("honey") : NeoForgeMod.MILK.get(), state.getValue(LEVEL) * 250));
        }
    }
}
