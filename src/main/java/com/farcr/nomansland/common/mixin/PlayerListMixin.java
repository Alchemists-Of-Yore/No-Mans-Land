package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(
        method = "placeNewPlayer",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;",
            shift = At.Shift.AFTER
        )
    )
    private void nml$resetPositionOnRejoin(
        Connection connection, ServerPlayer player,
        CommonListenerCookie cookie, CallbackInfo ci,
        @Local ResourceKey<Level> resourceKey
    ) {
        Optional<DreamType> previousDream = DreamLevelHandler.keyToDream(resourceKey);
        previousDream.ifPresent((dreamType) -> DreamLevelHandler.playerTeleportFallback(player, true));
    }
}
