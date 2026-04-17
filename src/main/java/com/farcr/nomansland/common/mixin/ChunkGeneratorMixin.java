package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.extension.ChunkGeneratorStructureStateExtension;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
    @Inject(method = "createState", at = @At("RETURN"))
    private void nomansland$attachSelfToState(HolderLookup<StructureSet> structureSetLookup, RandomState randomState, long seed, CallbackInfoReturnable<ChunkGeneratorStructureState> cir) {
        ChunkGeneratorStructureState state = cir.getReturnValue();
        if (state instanceof ChunkGeneratorStructureStateExtension extension) {
            extension.nomansland$setChunkGenerator((ChunkGenerator) (Object) this);
        }
    }
}
