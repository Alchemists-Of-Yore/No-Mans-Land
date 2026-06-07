package com.farcr.nomansland.common.mixin;

import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(JigsawPlacement.Placer.class)
public interface JigsawPlacerInvoker {
    @Invoker("tryPlacingChildren")
    void nml$tryPlacingChildren(
            PoolElementStructurePiece piece,
            MutableObject<VoxelShape> free,
            int depth,
            boolean useExpansionHack,
            LevelHeightAccessor level,
            RandomState randomState,
            PoolAliasLookup poolAliasLookup,
            LiquidSettings liquidSettings
    );
}
