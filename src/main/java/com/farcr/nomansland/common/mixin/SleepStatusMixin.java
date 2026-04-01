package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.dreams.DreamManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(SleepStatus.class)
public class SleepStatusMixin {
    @ModifyArg(method = "areEnoughDeepSleeping", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private Predicate<? super ServerPlayer> nml$modifySleepValue(Predicate<? super ServerPlayer> predicate) {
        return (player) -> predicate.test(player) && !DreamManager.getOrDefault(player.getServer()).playerShouldDream(player);
    }
}
