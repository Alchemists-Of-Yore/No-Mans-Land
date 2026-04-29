package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.structure.MeetingPointStructure;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(Beardifier.class)
public abstract class BeardifierMixin {
    @WrapOperation(
        method = "lambda$forStructuresInChunk$2",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/world/level/levelgen/structure/TerrainAdjustment;I)Lnet/minecraft/world/level/levelgen/Beardifier$Rigid;",
            ordinal = 1
        )
    )
    private static Beardifier.Rigid nml$disableMenhirBearding(
        BoundingBox box,
        TerrainAdjustment adjustment,
        int groundLevelDelta,
        Operation<Beardifier.Rigid> original,
        @Local(argsOnly = true) StructureStart start,
        @Local StructurePiece piece
    ) {
        if (start.getStructure() instanceof MeetingPointStructure) {
            List<StructurePiece> pieces = start.getPieces();
            if (!pieces.isEmpty() && piece != pieces.get(0)) {
                return original.call(box, TerrainAdjustment.BEARD_THIN, groundLevelDelta);
            }
        }
        return original.call(box, adjustment, groundLevelDelta);
    }
}
