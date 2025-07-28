package com.farcr.nomansland.common.effect;

import com.farcr.nomansland.common.entity.PacifiedAttackGoal;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class PacifiedEffect extends MobEffect {
    public PacifiedEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectAdded(LivingEntity livingEntity, int amplifier) {
        super.onEffectAdded(livingEntity, amplifier);

        if (livingEntity instanceof Monster monster) {
            for (WrappedGoal goal : monster.targetSelector.getAvailableGoals()) {
                if (goal.getGoal() instanceof PacifiedAttackGoal) {
                    return;
                }
            }

            monster.targetSelector.addGoal(0, new PacifiedAttackGoal(monster));
        } else {
            if (livingEntity instanceof NeutralMob neutralMob) neutralMob.stopBeingAngry();
            livingEntity.removeEffect(NMLEffects.PACIFIED);
        }
    }
}
