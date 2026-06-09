package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.world.saved_data.PreservedStructureData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelMixin {

    @Inject(method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z", at = @At("HEAD"), cancellable = true)
    private void nml$preventPreservedDestroy(final BlockPos pos, final boolean dropBlock, @Nullable final Entity entity, final int recursionLeft, final CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof final ServerLevel serverLevel && PreservedStructureData.get(serverLevel).isPreserved(pos.asLong())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "removeBlock", at = @At("HEAD"), cancellable = true)
    private void nml$preventPreservedRemove(final BlockPos pos, final boolean isMoving, final CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof final ServerLevel serverLevel && PreservedStructureData.get(serverLevel).isPreserved(pos.asLong())) {
            cir.setReturnValue(false);
        }
    }
}
