package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.saved_data.WardedSpacesData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.PatrolSpawner;
import net.minecraft.world.level.saveddata.SavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PatrolSpawner.class)
public class PatrolSpawnerMixin {
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;isCloseToVillage(Lnet/minecraft/core/BlockPos;I)Z"))
    private boolean isWarded(ServerLevel instance, BlockPos pos, int sections, Operation<Boolean> original) {
        WardedSpacesData wardedSpacesData = instance.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                WardedSpacesData::new, WardedSpacesData::create), WardedSpacesData.NAME);

        if (wardedSpacesData.isWarded(pos)) {
            return true;
        } else return original.call(instance, pos, sections);
    }
}
