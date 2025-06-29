package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.generation.fogsea.FogSeaGenerator;
import net.minecraft.core.Holder;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseBasedChunkGenerator.class)
public abstract class NoiseBasedChunkGeneratorMixin {
    @Shadow public abstract Holder<NoiseGeneratorSettings> generatorSettings();

    @Inject(method = "doFill(Lnet/minecraft/world/level/levelgen/blending/Blender;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/levelgen/RandomState;Lnet/minecraft/world/level/chunk/ChunkAccess;II)Lnet/minecraft/world/level/chunk/ChunkAccess;",
            at = @At("RETURN"))
    public void nml$doFillEnd(Blender blender, StructureManager structureManager, RandomState random, ChunkAccess chunk, int minCellY, int cellCountY, CallbackInfoReturnable<ChunkAccess> cir) {
        // todo:
        //  -   replace with a better check, maybe based off dimension id ???
        //    definitely have it be configurable.
        //    will be a real pain in the ass in any case. Thanks mojang.
        //  -   don't calculate border noise in areas not on the border.
        //    this won't be too bad... probably.
        //    will make this much less terrible and slow!
        if (this.generatorSettings().is(NoiseGeneratorSettings.OVERWORLD)) {
            FogSeaGenerator.fillFogSeaNoise(chunk, generatorSettings().value().defaultBlock());
        }
    }

    @Inject(method = "createFluidPicker(Lnet/minecraft/world/level/levelgen/NoiseGeneratorSettings;)Lnet/minecraft/world/level/levelgen/Aquifer$FluidPicker;",
            at = @At("RETURN"), cancellable = true)
    private static void nml$fluidPicker(NoiseGeneratorSettings settings, CallbackInfoReturnable<Aquifer.FluidPicker> cir) {
        int seaLevel = settings.seaLevel();
        int lavaSeaLevel = -54;
        Aquifer.FluidStatus lavaFluid = new Aquifer.FluidStatus(lavaSeaLevel, Blocks.LAVA.defaultBlockState());
        Aquifer.FluidStatus oceanFluid = new Aquifer.FluidStatus(seaLevel, settings.defaultFluid());
        
        // todo:
        //  -   fix this being Pretty Bad.
        //    currently, this replaces fluids in all dimensions. This is pretty bad.
        //    maybe redirect this method entirely to do something less terrible?
        //    hurts compatibility but Whatever. not like anyone else is bothering to
        //    make changes this extensive to the noise-based world-gen code.
        //    it's a mess anyway.
        Aquifer.FluidStatus airFluidStatus = new Aquifer.FluidStatus(0, Blocks.AIR.defaultBlockState());
        cir.setReturnValue((x, y, z) -> FogSeaGenerator.isInFogSea(x, z) ? airFluidStatus :
                (y < Math.min(lavaSeaLevel, seaLevel) ? lavaFluid : oceanFluid) // the default fluid picker
        );
    }
}
