package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.extension.EntityExtension;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin implements EntityExtension {

    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Shadow public abstract double getY();

    @Shadow public abstract double getY(double scale);

    @Shadow public abstract double getZ();

    @Shadow public abstract double getX();

    @Shadow public abstract Level level();

    @Shadow public abstract EntityType<?> getType();

    @Shadow @Final protected RandomSource random;

    @Shadow public abstract BlockPos blockPosition();

    @Shadow public abstract Set<String> getTags();

    @Shadow public abstract BlockPos getOnPos();

    @Shadow protected abstract BlockPos getOnPos(float yOffset);

    @Shadow public abstract BlockState getBlockStateOn();

    @Shadow public float fallDistance;

    @Shadow public abstract Vec3 position();

    @Shadow public abstract boolean onGround();

    @Shadow public abstract boolean isInWater();

    /*
    * Offering Injection
    */

    @Shadow public abstract float getYRot();

    @Unique private boolean NML$offering = false;
    @Unique private boolean NML$previouslyInspected = false;
    public void NML$setInspectionState(boolean isInspecting) {
        NML$offering = isInspecting;
        if (isInspecting)
            NML$previouslyInspected = true;
    }

    public boolean NML$isBeingInspected() {
        return NML$offering;
    }

    public boolean NML$wasPreviouslyInspected() {
        return NML$previouslyInspected;
    }

    @Inject(method = "getGravity", at = @At("RETURN"), cancellable = true)
    private void NML$getGravity(CallbackInfoReturnable<Double> cir) {
        if (NML$isBeingInspected()) cir.setReturnValue(0.0d);
    }

    @Unique @Nullable
    private Vec3 startingToFallPosition;

    @Inject(method = "getOnPosLegacy", at = @At("RETURN"), cancellable = true)
    private void getOnPosLegacy(CallbackInfoReturnable<BlockPos> cir) {
        cir.setReturnValue(getBlockStateOn().is(NMLBlocks.SPIKE_TRAP.block()) ? getOnPos() : getOnPos(0.2F));
    }

    @Inject(method = "resetFallDistance", at = @At("HEAD"))
    private void resetFallDistance(CallbackInfo ci) {
        if (((Entity) (Object) this) instanceof LivingEntity livingEntity && livingEntity.getHealth() > 0 && startingToFallPosition != null && !livingEntity.getPassengers().isEmpty()) {
            livingEntity.getPassengers().forEach(entity -> {
                if (entity instanceof ServerPlayer player && player.getHealth() > 0) {
                    CriteriaTriggers.FALL_FROM_HEIGHT.trigger(player, this.startingToFallPosition);
                }
            });
        }

        startingToFallPosition = null;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        trackStartFallingPosition();
    }

    @Unique
    private void trackStartFallingPosition() {
        if (fallDistance > 0.0F && startingToFallPosition == null) {
            startingToFallPosition = position();
        }
    }
}
