package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.densityfunction.modification.DensityFunctionRegistryCodec;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RegistryFileCodec.class)
public class RegistryFileCodecMixin {
    @Inject(method = "create(Lnet/minecraft/resources/ResourceKey;Lcom/mojang/serialization/Codec;)Lnet/minecraft/resources/RegistryFileCodec;", at = @At("HEAD"), cancellable = true)
    private static <E> void nml$modifyDensityFunctionRegistryCodec(ResourceKey<? extends Registry<E>> registryKey, Codec<E> elementCodec, CallbackInfoReturnable<RegistryFileCodec<E>> cir) {
        if (Registries.DENSITY_FUNCTION.equals(registryKey)) {
            cir.setReturnValue((RegistryFileCodec<E>) new DensityFunctionRegistryCodec((Codec<DensityFunction>) elementCodec));
        }
    }
}
