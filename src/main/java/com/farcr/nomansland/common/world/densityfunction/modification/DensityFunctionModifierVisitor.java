package com.farcr.nomansland.common.world.densityfunction.modification;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

import java.util.function.Function;

// unused for now
class DensityFunctionModifierVisitor implements DensityFunction.Visitor {
    private final DensityFunctionModifier modifier;
    private boolean modified = false;

    public DensityFunctionModifierVisitor(DensityFunctionModifier modifier) {
        this.modifier = modifier;
    }

    @Override
    public DensityFunction apply(DensityFunction densityFunction) {
        if (modified)
            return densityFunction;
        if (densityFunction instanceof DensityFunctions.MarkerOrMarked)
            return densityFunction;


        // only modify the outermost density function.
        modified = true;
        return modifier.apply(densityFunction);
    }
}
