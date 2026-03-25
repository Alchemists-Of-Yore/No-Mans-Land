package com.farcr.nomansland.common.mixin;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.registry.NMLDamageTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "getHurtSound", at = @At("HEAD"), cancellable = true)
    private void nml$getHurtSound(DamageSource source, CallbackInfoReturnable<SoundEvent> info) {
        if (source.is(NMLDamageTypes.SPIKE_POKE)) info.setReturnValue(NMLSounds.PLAYER_HURT_SPIKE_TRAP.get());
    }

    @Unique private Player nml$Self = ((Player) (Object) this);

    @Unique private DreamType nml$getAmbiguousDreamType() {
        if (nml$Self instanceof ServerPlayer player) return DreamManager.getOrDefault(player.getServer()).playerGetDream(player);
        return null;
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void nml$noSprint(CallbackInfo ci) {
        DreamType dreamType = nml$getAmbiguousDreamType();
        if (dreamType != null && !dreamType.canSprint && !nml$Self.isCreative())
            nml$Self.setSprinting(false);
    }
    
}