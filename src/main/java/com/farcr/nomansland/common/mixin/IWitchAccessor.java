package com.farcr.nomansland.common.mixin;

import net.minecraft.world.entity.monster.Witch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Witch.class)
public interface IWitchAccessor {
    @Accessor void setUsingTime(int usingTime);
}
