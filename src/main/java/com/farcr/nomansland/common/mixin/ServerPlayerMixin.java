package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.registry.NMLCriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends LivingEntityMixin {

    @Unique
    private Vec3 startingToTopPosition;

    @Inject(method = "tick", at = @At("HEAD"))
    private void nml$tick(CallbackInfo ci) {
        this.nml$updateClimbing();
    }

    @Unique
    public void nml$updateClimbing() {
        if (level().getBlockState(blockPosition()).is(Blocks.LADDER) && !onGround()) {
            if (startingToTopPosition != null && startingToTopPosition.vectorTo(position()).y > 0) startingToTopPosition = startingToTopPosition.vectorTo(position());
            else startingToTopPosition = position();
        } else if (startingToTopPosition != null) {
            NMLCriteriaTriggers.CLIMB_UP_HEIGHT.get().trigger(((ServerPlayer) (Object) this), startingToTopPosition);

            startingToTopPosition = null;
        }
    }
}
