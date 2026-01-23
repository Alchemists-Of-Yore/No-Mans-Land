package com.farcr.nomansland.common.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class CuringBandageItem extends BandageItem {
    private List<Holder<MobEffect>> curableEffects;

    public CuringBandageItem(List<Holder<MobEffect>> curableEffects) {
        super(new Properties());
        this.curableEffects = curableEffects;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        Player player = livingEntity instanceof Player ? (Player) livingEntity : null;
        Collection<MobEffectInstance> effects = player.getActiveEffects();
        List<Holder<MobEffect>> effectsToRemove = new ArrayList();
        for (MobEffectInstance effect : effects) {
            if (curableEffects.contains(effect.getEffect())) {
                effectsToRemove.add(effect.getEffect());
            }
        }
        for (Holder<MobEffect> effect : effectsToRemove) {
            player.removeEffect(effect);
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }
}
