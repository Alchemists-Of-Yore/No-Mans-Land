package com.farcr.nomansland.common.world.densityfunction.modification;

import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;

import java.util.function.Function;

public interface DensityFunctionModifier extends Function<DensityFunction, DensityFunction> {
    default DensityFunctionModifier combine(DensityFunctionModifier other) {
        return (t) -> this.apply(other.apply(t));
    }

    default DensityFunction visit(DensityFunction densityFunction) {
//        DensityFunctionModifierVisitor visitor = new DensityFunctionModifierVisitor(this);
//        return visitor.apply(densityFunction);
        if (densityFunction instanceof DensityFunctions.Marker marker) {
            return new DensityFunctions.Marker(marker.type(), this.visit(marker.wrapped()));
        }
        return this.apply(densityFunction);
    }
}
