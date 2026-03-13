package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.extension.LivingEntityExtension;
import com.farcr.nomansland.common.handler.InvertedBellServerHandler;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends EntityMixin implements LivingEntityExtension {

    @Shadow public abstract boolean hasEffect(Holder<MobEffect> effect);

    @Shadow public abstract void setJumping(boolean jumping);

    @Shadow public boolean jumping;
    @Unique
    private boolean nomansland$skipDroppingDeathLoot = false;
    @Unique
    private int nml$bellParalysisTimer = 0;

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

    @Override
    public void nml$beginBellParalysis() {
        this.nml$bellParalysisTimer = InvertedBellServerHandler.TELEPORT_ENTITY_TIME * 2;
        this.setJumping(false);
    }

    @Override
    public int nml$getBellParalysis() {
        return this.nml$bellParalysisTimer;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void nml$countDownParalysis(CallbackInfo ci) {
        if (this.nml$bellParalysisTimer > 0) {
            this.nml$bellParalysisTimer--;
        }
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void nml$makeParalyzedTravel(Vec3 travelVector, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Vec3> travelVectorr) {
        if (this.nml$bellParalysisTimer > 0) {
            this.jumping = false;
            travelVectorr.set(Vec3.ZERO);
        }
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 0))
    private void nml$makeParalyzedJump(CallbackInfo ci) {
        if (this.nml$bellParalysisTimer > 0) {
            this.jumping = false;
        }
    }

    @Inject(method = "isImmobile", at = @At("RETURN"), cancellable = true)
    private void nml$makeParalyzedImmobile(CallbackInfoReturnable<Boolean> cir) {
        // immobilized players bypass the arm swing which loops odd :p
        if (this.nml$bellParalysisTimer > 0 && !((Object)this instanceof Player)) {
            cir.setReturnValue(true);
        }
    }
}
