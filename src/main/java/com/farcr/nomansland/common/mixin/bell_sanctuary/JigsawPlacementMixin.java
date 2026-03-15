package com.farcr.nomansland.common.mixin.bell_sanctuary;

import com.farcr.nomansland.common.world.structure.bell_sanctuary.CenteredSinglePoolElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(JigsawPlacement.class)
public class JigsawPlacementMixin {

    @WrapOperation(method = "Lnet/minecraft/world/level/levelgen/structure/pools/JigsawPlacement;addPieces(Lnet/minecraft/world/level/levelgen/structure/Structure$GenerationContext;Lnet/minecraft/core/Holder;Ljava/util/Optional;ILnet/minecraft/core/BlockPos;ZLjava/util/Optional;ILnet/minecraft/world/level/levelgen/structure/pools/alias/PoolAliasLookup;Lnet/minecraft/world/level/levelgen/structure/pools/DimensionPadding;Lnet/minecraft/world/level/levelgen/structure/templatesystem/LiquidSettings;)Ljava/util/Optional;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;subtract(Lnet/minecraft/core/Vec3i;)Lnet/minecraft/core/BlockPos;", ordinal = 1))
    private static BlockPos nml$includeOffset(final BlockPos instance, final Vec3i vector, final Operation<BlockPos> original,
                                              @Local final StructurePoolElement element, @Local final StructureTemplateManager manager, @Local final Rotation rotation) {
        if (element instanceof final CenteredSinglePoolElement centeredPool) {
            return original.call(instance, vector).offset(centeredPool.getOffset(manager, instance, rotation));
        }

        return original.call(instance, vector);
    }

}
