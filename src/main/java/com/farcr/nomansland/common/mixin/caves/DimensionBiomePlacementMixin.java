package com.farcr.nomansland.common.mixin.caves;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.llamalad7.mixinextras.sugar.Local;
import com.terraformersmc.biolith.api.biome.BiolithFittestNodes;
import com.terraformersmc.biolith.impl.biome.DimensionBiomePlacement;
import com.terraformersmc.biolith.impl.biome.OverworldBiomePlacement;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionBiomePlacement.class)
public class DimensionBiomePlacementMixin {
    @Inject(method = "getReplacement", at = @At("RETURN"), cancellable = true)
    private void addCaveReplacement(int x, int y, int z, Climate.TargetPoint noisePoint, BiolithFittestNodes<Holder<Biome>> fittestNodes, CallbackInfoReturnable<Holder<Biome>> cir, @Local Holder<Biome> biomeEntry)
    {
        if (NMLConfig.CAVES_BIOMES.get()) {
            if ((DimensionBiomePlacement)(Object)this instanceof OverworldBiomePlacement) {
                if (!biomeEntry.is(Tags.Biomes.IS_CAVE)) {
                    if (noisePoint.depth() > 0.5f * 10000.0f) {
                        cir.setReturnValue(NMLBiomes.CAVE_DEPTHS_HOLDER);
                    } else if (noisePoint.depth() > 0.1f * 10000.0f) {
                        cir.setReturnValue(NMLBiomes.CAVES_HOLDER);
                    }
                }
            }
        }
    }
}
