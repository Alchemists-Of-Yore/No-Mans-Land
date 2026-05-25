package com.farcr.nomansland.common.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.WitchModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WitchModel.class)
public class WitchModelMixin {
    @WrapOperation(method = "setupAnim", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/model/WitchModel;holdingItem:Z", opcode = Opcodes.GETFIELD))
    private boolean replaceHoldingItemCheck(WitchModel<?> instance, Operation<Boolean> original, Entity entity) {
        if (entity instanceof LivingEntity living && living.getMainHandItem().is(Items.BOWL)) return false;
        return original.call(instance);
    }
}
