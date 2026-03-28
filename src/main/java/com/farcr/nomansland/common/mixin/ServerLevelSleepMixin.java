package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.dreams.DreamManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;

/*
* Separate to avoid exclusion when snowrealmagic is on
*/
@Mixin(ServerLevel.class)
public abstract class ServerLevelSleepMixin {

    @Shadow
    @Nonnull
    public abstract MinecraftServer getServer();

    @ModifyExpressionValue(method = "updateSleepingPlayerList", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/SleepStatus;update(Ljava/util/List;)Z"))
    private boolean nml$updateSleepingPlayerList(boolean original) {
        if (DreamManager.IGNORE_UPDATE_CONTEXT) {
            DreamManager.IGNORE_UPDATE_CONTEXT = false;
            return false;
        }
        return original;
    }

    @Inject(method = "updateSleepingPlayerList", at = @At("TAIL"))
    private void nml$updateIgnoreContextRegardless(CallbackInfo ci) {
        DreamManager.IGNORE_UPDATE_CONTEXT = false;
    }

    @ModifyExpressionValue(method = "announceSleepStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/SleepStatus;amountSleeping()I"))
    private int nml$InjectSleepingAmount(int original) {
        return (original + DreamManager.getOrDefault(getServer()).getDreamingPlayerCount());
    }

    /*
    * Doing it like this to avoid conflicts with anything that replaces sleepersNeeded
    *
    * and also because im lazy
    */
    @WrapOperation(method = "announceSleepStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/SleepStatus;sleepersNeeded(I)I"))
    private int nml$InjectSleepingNeeded(SleepStatus instance, int requiredSleepPercentage, Operation<Integer> original) {
        int extendedCount = DreamManager.getOrDefault(getServer()).getDreamingPlayerCount();
        instance.activePlayers += extendedCount;
        int result = original.call(instance, requiredSleepPercentage);
        instance.activePlayers -= extendedCount;
        return result;
    }
}
