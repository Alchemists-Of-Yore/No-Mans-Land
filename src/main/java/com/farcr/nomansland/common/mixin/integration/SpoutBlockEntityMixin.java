package com.farcr.nomansland.common.mixin.integration;

import com.farcr.nomansland.common.mixin.plugin.annotation.IfModPresent;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.spout.SpoutBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@IfModPresent("create")
@Mixin(SpoutBlockEntity.class)
public abstract class SpoutBlockEntityMixin {

    @Shadow protected abstract boolean trackFoods();

    @Shadow private boolean createdHoneyApple;

    @Inject(method = "whenItemHeld", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/foundation/fluid/SmartFluidTank;setFluid(Lnet/neoforged/neoforge/fluids/FluidStack;)V", shift = At.Shift.BEFORE))
    private void countNMLHoneyedApple(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler, CallbackInfoReturnable<BeltProcessingBehaviour.ProcessingResult> cir, @Local ItemStack out) {
        if (trackFoods()) {
            this.createdHoneyApple |= out.is(NMLItems.HONEYED_APPLE);
        }
    }
}
