package com.farcr.nomansland.common.item;

import com.farcr.nomansland.NMLConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

public class BandageHud {

    public static ItemStack held(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof BandageItem) return mainHand;
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof BandageItem) return offHand;
        return ItemStack.EMPTY;
    }

    public static boolean isHolding(Player player) {
        return !held(player).isEmpty();
    }

    public static FoodProperties food() {
        int duration = Math.max(1, (int) Math.round(NMLConfig.BANDAGE_HEAL_AMOUNT.get() * 50.0));
        return new FoodProperties.Builder()
                .alwaysEdible()
                .effect(new MobEffectInstance(MobEffects.REGENERATION, duration, 0), 1.0F)
                .build();
    }
}
