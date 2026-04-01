package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.DreamStorage;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.registry.NMLCriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Optional;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends LivingEntityMixin {

    @Shadow @Final public MinecraftServer server;

    @Unique ServerPlayer nml$Self = (ServerPlayer) (Object) this;

    @Inject(method = "stopSleepInBed", at = @At("HEAD"), cancellable = true)
    private void nml$stopSleeping(CallbackInfo ci) {
        if (DreamManager.getPlayerShouldDream(nml$Self))
            ci.cancel();
    }

    @Unique
    private Vec3 startingToTopPosition;

    @Inject(method = "tick", at = @At("HEAD"))
    private void nml$tick(CallbackInfo ci) {
        this.nml$updateClimbing();

        // Decrement player dream times whenever possible
        Optional<DreamStorage> storage = DreamManager.getOrDefault(server)
            .getPlayerStorageOptional(nml$Self);
        if (storage.isPresent()) {
            DreamStorage dreamStorage = storage.get();
            HashMap<DreamType, Integer> dreamStorageMap = dreamStorage.getDreamTimes();
            for (DreamType dreamType : dreamStorageMap.keySet()) {
                int currentTime = dreamStorage.getTimeRemainingForDream(dreamType);
                if (currentTime > 0) dreamStorage.setTimeRemainingForDream(dreamType, Math.max(currentTime - 1, 0));
            }
        }
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
