package com.farcr.nomansland.common.world.densityfunction.modification;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.DensityFunction;

import java.util.HashMap;
import java.util.Map;

public class DensityFunctionModifications {
    public static Map<ResourceKey<DimensionType>, NoiseRouterModifications> NOISE_ROUTER_MODIFICATIONS = new HashMap<>();
    public static Map<ResourceKey<DensityFunction>, DensityFunctionModifier> MODIFIERS = new HashMap<>();

    public static void addModifier(ResourceKey<DensityFunction> target, DensityFunctionModifier modifier) {
        if (MODIFIERS.containsKey(target)) {
            MODIFIERS.merge(target, modifier, DensityFunctionModifier::combine);
        } else {
            MODIFIERS.put(target, modifier);
        }
    }

    public static void addNoiseRouterParameterModifier(ResourceKey<DimensionType> targetDimension, NoiseRouterParameter targetParameter, DensityFunctionModifier modifier) {
        if (NOISE_ROUTER_MODIFICATIONS.containsKey(targetDimension)) {
            NOISE_ROUTER_MODIFICATIONS.get(targetDimension).addModifier(targetParameter, modifier);
        } else {
            NoiseRouterModifications modifications = new NoiseRouterModifications();
            modifications.addModifier(targetParameter, modifier);
            NOISE_ROUTER_MODIFICATIONS.put(targetDimension, modifications);
        }
    }

    public static class NoiseRouterModifications {
        public Map<NoiseRouterParameter, DensityFunctionModifier> modifiers = new HashMap<>();

        private NoiseRouterModifications() {}

        private void addModifier(NoiseRouterParameter target, DensityFunctionModifier modifier) {
            if (modifiers.containsKey(target)) {
                modifiers.merge(target, modifier, DensityFunctionModifier::combine);
            } else {
                modifiers.put(target, modifier);
            }
        }
    }
}
