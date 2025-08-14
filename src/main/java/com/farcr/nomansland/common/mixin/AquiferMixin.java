package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.mixinextensions.WatershepMapHolder;
import com.farcr.nomansland.common.world.watershed.River;
import com.farcr.nomansland.common.world.watershed.Watershed;
import com.farcr.nomansland.common.world.watershed.WatershedMap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Aquifer.NoiseBasedAquifer.class)
public class AquiferMixin {
    @Shadow @Final private NoiseChunk noiseChunk;

    @Inject(method =  "computeFluid", at = @At("HEAD"), cancellable = true)
    private void nml$computeRiverAquiferFluid(int x, int y, int z, CallbackInfoReturnable<Aquifer.FluidStatus> cir) {
        WatershedMap watershedMap = ((WatershepMapHolder) this.noiseChunk).nml$getWatershedMap();
        Watershed watershed = watershedMap.watershedAtBlock(x, z);

        Aquifer.FluidStatus fluidOverride = watershed.getFluidOverride(x, y, z);
        if (fluidOverride != null)
            cir.setReturnValue(fluidOverride);
//        if (watershed.hasRiver()) {
//            River river = watershed.river();
//            River.RiverSpaceCoordinates coordinates = river.getRiverSpaceCoordinates(x, y, z);
//            double distanceToWaterSurface = y - coordinates.riverHeight();
//            if (coordinates.horizontalDistance() < 30 && distanceToWaterSurface < 25 && distanceToWaterSurface > -30) {
//                cir.setReturnValue(new Aquifer.FluidStatus(River.getWaterSurfaceHeight(coordinates.riverHeight(), 0), Blocks.WATER.defaultBlockState()));
//            }
//        }
    }
}
