package com.farcr.nomansland.common.entity.ai;

import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;

public class PacifiedAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {

    public PacifiedAttackGoal(Mob mob) {
        super(mob, LivingEntity.class, 0, true, true, PacifiedAttackGoal::isHostile);
    }

    private static boolean isHostile(LivingEntity livingEntity) {
        return livingEntity instanceof Enemy && !(livingEntity instanceof Creeper) && !livingEntity.hasEffect(NMLEffects.PACIFIED);
    }

    @Override
    public boolean canUse() {
        return mob.hasEffect(NMLEffects.PACIFIED);
    }

    @Override
    public boolean canContinueToUse() {
        return mob.hasEffect(NMLEffects.PACIFIED);
    }
}
