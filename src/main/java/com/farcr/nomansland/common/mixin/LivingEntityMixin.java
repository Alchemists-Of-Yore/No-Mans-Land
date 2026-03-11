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

    @Unique
    private boolean nomansland$skipDroppingDeathLoot = false;

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

    @Unique
    private int nml$bellParalysisTimer = 0;

    @Override
    public void nml$beginBellParalysis() {
        this.nml$bellParalysisTimer = InvertedBellServerHandler.TELEPORT_ENTITY_TIME * 2;
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
    private void nml$makeParalyzed(Vec3 travelVector, CallbackInfo ci, @Local(argsOnly = true) LocalRef<Vec3> travelVectorr) {
        if (this.nml$bellParalysisTimer > 0) {
            travelVectorr.set(Vec3.ZERO);
        }
    }
}
