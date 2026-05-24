package com.farcr.nomansland.common.entity.ai;

import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;

public class PacifiedAttackGoal extends NearestAttackableTargetGoal<Mob> {

    public PacifiedAttackGoal(Mob mob) {
        super(mob, Mob.class, 5, false, false, PacifiedAttackGoal::isHostile);
    }

    private static boolean isHostile(LivingEntity livingEntity) {
        return livingEntity instanceof Enemy && !(livingEntity instanceof Creeper) && !livingEntity.hasEffect(NMLEffects.PACIFIED);
    }

    @Override
    public boolean canUse() {
        super.canUse();
        return mob.hasEffect(NMLEffects.PACIFIED);
    }

    @Override
    public boolean canContinueToUse() {
        return mob.hasEffect(NMLEffects.PACIFIED) && super.canContinueToUse() && isHostile(mob.getTarget());
    }
}
