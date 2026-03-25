package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.registry.NMLDreamTypes;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Registry;
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
        Registry<DreamType> dreamRegistry = NMLDreamTypes.DREAM_TYPES_REGISTRY.getRegistry().get();
        Optional<DreamType> previousDream = dreamRegistry.stream().filter(
            (dreamType) -> resourceKey.location()
                .getPath().contains(dreamRegistry.getKey(dreamType).getPath())
        ).findFirst();
        previousDream.ifPresent((dreamType) -> {
            NoMansLand.LOGGER.info("Teleporting " + player.getGameProfile().getName() + " from " + previousDream + " to Respawn Point as a last resort! Did the server crash previously?");
            player.setPos(player.getRespawnPosition().getCenter());
        });
    }
}
