package com.farcr.nomansland.common.mixin.bell_sanctuary;

import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryCell;
import com.farcr.nomansland.common.handler.sanctuary_grid.BellSanctuaryGridHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin that attempts to generate a {@link BellSanctuaryCell cell} when a new chunk is generated. If a cell already exists, does nothing.
 */
@Mixin(ChunkGenerator.class)
public class GenerateCell {

    @Inject(method = "createStructures", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/SectionPos;bottomOf(Lnet/minecraft/world/level/chunk/ChunkAccess;)Lnet/minecraft/core/SectionPos;"))
    public void nomansdland$generateCell(final RegistryAccess registryAccess,
                                        final ChunkGeneratorStructureState structureState,
                                        final StructureManager structureManager,
                                        final ChunkAccess chunk,
                                        final StructureTemplateManager structureTemplateManager,
                                        final CallbackInfo ci,
                                        @Local final ChunkPos pos) {
        BellSanctuaryGridHandler.generateCell(structureState.getLevelSeed(), pos.x, pos.z);
    }

}
