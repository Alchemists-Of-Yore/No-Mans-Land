package com.farcr.nomansland.common.mixin.client;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // https://bugs-legacy.mojang.com/browse/MC-273361
    // literally fixed in the first snapshot after 1.21.1
    // i am crying

    // client bound move entity packet calls this
    // this only has an effect if the entity is ticking clientside
    // if the entity is reallly far away then it will never *move* close enough to start ticking
    @Inject(method = "lerpTo", at = @At("HEAD"))
    private void nml$fixMC273361(double x, double y, double z, float yRot, float xRot, int steps, CallbackInfo ci) {
        if (this.levelCallback instanceof TransientEntitySectionManager.Callback manager) {
            if (!manager.currentSection.getStatus().isTicking()) {
                this.setPos(x, y, z);
                this.setRot(yRot, xRot);
            }
        }
    }
}
