package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.integration.FDIntegration;
import com.farcr.nomansland.common.integration.Mods;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Witch.class)
public abstract class WitchMixin extends Raider {
    @Shadow private int usingTime;

    @Shadow public abstract void setUsingItem(boolean usingItem);

    @Shadow public abstract boolean isDrinkingPotion();

    protected WitchMixin(EntityType<? extends Raider> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Witch;isDrinkingPotion()Z", shift = At.Shift.BEFORE))
    private void eatStew(CallbackInfo ci){
        if (Mods.FARMERSDELIGHT.isLoaded() && this.getMainHandItem().is(FDIntegration.WITCH_STEW_ITEM)) {
            if (this.usingTime < 32 && !this.isSilent() && this.usingTime % 7 == 0) {
                this.playSound(SoundEvents.GENERIC_EAT, 0.6F, 0.8F + this.random.nextFloat() * 0.4F);
                if (this.level() instanceof ServerLevel serverLevel) {
                    for(int i = 0; i < 8; ++i) {
                        serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, this.getMainHandItem()), this.getX() + this.getLookAngle().x / 2.0, this.getEyeY() - 0.15, this.getZ() + this.getLookAngle().z / 2.0, 1, 0, 0, 0, 0);
                    }
                }
            }
            if (this.usingTime-- <= 0) {
                if (!this.isSilent()) {
                    this.playSound(SoundEvents.PLAYER_BURP, 0.6F, 0.65F + this.random.nextFloat() * 0.4F);
                }
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.setHealth(this.getMaxHealth());
                this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200));
                this.gameEvent(GameEvent.EAT);
            }
        }
    }
}
