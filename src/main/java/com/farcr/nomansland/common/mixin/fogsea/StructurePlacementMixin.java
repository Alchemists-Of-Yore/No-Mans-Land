package com.farcr.nomansland.common.mixin.fogsea;

import com.farcr.nomansland.common.world.generation.fogsea.FogSeaGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructurePlacement.class)
public class StructurePlacementMixin {
    @Inject(method = "isStructureChunk", at = @At("RETURN"), cancellable = true)
    private void applyFogSeaRestriction(ChunkGeneratorStructureState structureState, int x, int z, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            cir.setReturnValue(!FogSeaGenerator.isInFogSea(x * 16 + 8, z * 16 + 8));
        }
    }
}
