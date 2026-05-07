package com.farcr.nomansland.common.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import vectorwing.farmersdelight.common.item.DrinkableItem;

public class PestoBottleItem extends DrinkableItem {

    public PestoBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_DRINK;
    }
}
