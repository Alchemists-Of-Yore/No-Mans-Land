package com.farcr.nomansland.common.mixin;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.dreamtypes.MoonlightDreamType;
import com.farcr.nomansland.common.registry.NMLDamageTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
    private void nml$getHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> info) {
        if (source.is(NMLDamageTypes.SPIKE_POKE)) info.setReturnValue(NMLSounds.PLAYER_HURT_SPIKE_TRAP.get());
    }

    @Unique private Player nml$Self = ((Player) (Object) this);

    @Inject(method = "stopSleepInBed", at = @At("HEAD"), cancellable = true)
    private void nml$stopSleeping(CallbackInfo ci) {
        if (DreamManager.getPlayerShouldDream(nml$Self))
            ci.cancel();
    }

    @Inject(method = "isImmobile", at = @At("RETURN"), cancellable = true)
    private void nml$playerImmobile(CallbackInfoReturnable<Boolean> cir) {
        DreamType.DreamTypeInstance dreamTypeInstance = DreamManager.getAmbiguousDreamTypeInstance(nml$Self);
        if (dreamTypeInstance instanceof MoonlightDreamType.MoonlightDreamTypeInstance moonlightDreamType
        && moonlightDreamType.moonPresenceTime > 0) cir.setReturnValue(true);
    }

    @Inject(method = "mayUseItemAt", at = @At("RETURN"), cancellable = true)
    private void nml$cancelInteraction(BlockPos pos, Direction facing, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (DreamManager.getAmbiguousDreamType(nml$Self) != null) cir.setReturnValue(false);
    }

    @Inject(method = "blockActionRestricted", at = @At("RETURN"), cancellable = true)
    private void nml$cancelAction(Level level, BlockPos pos, GameType gameMode, CallbackInfoReturnable<Boolean> cir) {
        if (DreamManager.getAmbiguousDreamType(nml$Self) != null) cir.setReturnValue(true);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void nml$noSprint(CallbackInfo ci) {
        DreamType dreamType = DreamManager.getAmbiguousDreamType(nml$Self);
        if (dreamType != null && !dreamType.canSprint && !nml$Self.isCreative())
            nml$Self.setSprinting(false);
    }
}