package com.farcr.nomansland.common.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CuringBandageItem extends BandageItem {
    private final List<Holder<MobEffect>> curableEffects;

    public CuringBandageItem(Properties properties, List<Holder<MobEffect>> curableEffects) {
        super(properties);
        this.curableEffects = curableEffects;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity livingEntity) {
        LivingEntity target = getTargetEntity(livingEntity);
        
        Collection<MobEffectInstance> effects = target.getActiveEffects();
        List<Holder<MobEffect>> effectsToRemove = new ArrayList<>();
        for (MobEffectInstance effect : effects) {
            if (curableEffects.contains(effect.getEffect())) {
                effectsToRemove.add(effect.getEffect());
            }
        }
        for (Holder<MobEffect> effect : effectsToRemove) {
            target.removeEffect(effect);
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }
}
