package com.farcr.nomansland.common.integration.create;

import com.farcr.nomansland.common.registry.items.NMLItems;
import com.simibubi.create.api.effect.OpenPipeEffectHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;

public class ResinOilEffectHandler implements OpenPipeEffectHandler {
    @Override
    public void apply(Level level, AABB area, FluidStack fluidStack) {
        PotionContents contents = NMLItems.RESIN_OIL_BOTTLE.stack().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        if (contents != PotionContents.EMPTY) {
            for(LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAffectedByPotions)) {
                contents.forEachEffect((instance) -> {
                    MobEffect effect = instance.getEffect().value();
                    if (effect.isInstantenous()) {
                        effect.applyInstantenousEffect(null, null, entity, instance.getAmplifier(), 0.5F);
                    } else entity.addEffect(new MobEffectInstance(instance));
                });
            }
        }
    }
}
