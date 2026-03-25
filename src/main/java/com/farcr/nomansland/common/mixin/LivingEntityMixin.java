package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamingPlayer;
import com.farcr.nomansland.common.extension.LivingEntityExtension;
import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements LivingEntityExtension {

    @Shadow public abstract boolean hasEffect(Holder<MobEffect> effect);

    @Shadow
    public abstract boolean isSleeping();

    @Unique private LivingEntity nml$Self = (LivingEntity) (Object) this;

    @Unique
    private boolean nomansland$skipDroppingDeathLoot = false;

    @Inject(method = "startSleeping", at = @At("TAIL"))
    private void nml$startSleeping(CallbackInfo ci) {
        if ((nml$Self instanceof ServerPlayer player) && DreamManager.getOrDefault(player.getServer()).playerShouldDream(player))
            DreamManager.getOrDefault(player.getServer()).notifyClient(player);
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void nml$transferSleep(CallbackInfo ci) {
        if (this.isSleeping() && nml$Self instanceof ServerPlayer player
            && DreamManager.getOrDefault(player.getServer()).playerShouldDream(player)
            && player.isSleepingLongEnough()
        ) {
            DreamManager manager = DreamManager.getOrDefault(player.getServer());
            DreamType dreamType = manager.playerGetDream(player);
            ServerLevel dreamLevel = DreamLevelHandler.getDreamLevel(player.server, dreamType, player);
            // summon fake player
            manager.createDreamingPlayer(player);
            // move player to other dimension
            player.stopSleeping();
            player.changeDimension(
                new DimensionTransition(
                    dreamLevel, dreamType.spawnPoint,
                    dreamType.spawnPoint, 0, 0,
                    DimensionTransition.DO_NOTHING
                )
            );
        }
    }

    @Override
    public void nml$skipDroppingDeathLoot() {
        this.nomansland$skipDroppingDeathLoot = true;
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    private void trySkipDroppingDeathLoot(ServerLevel p_level, DamageSource damageSource, CallbackInfo ci) {
        if (this.nomansland$skipDroppingDeathLoot) {
            ci.cancel();
        }
    }

    @ModifyVariable(method = "travel", at = @At("STORE"), ordinal = 0)
    public float tryReduceFriction(float value) {
        return hasEffect(NMLEffects.FLAMMABLE) && !isInWater() && onGround() ? 0.98F : value;
    }

    @Inject(method = "getBlockSpeedFactor", at = @At("RETURN"), cancellable = true)
    private void tryReduceBlockSpeedFactor(CallbackInfoReturnable<Float> cir) {
        if (hasEffect(NMLEffects.FLAMMABLE) && !isInWater()) cir.setReturnValue(0.95F);
    }
}
