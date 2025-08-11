package com.farcr.nomansland.common.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;

public class EnemyAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {

    public EnemyAttackGoal(Mob mob) {
        super(mob, LivingEntity.class, 0, true, true, EnemyAttackGoal::isHostile);
    }

    private static boolean isHostile(LivingEntity livingEntity) {
        return livingEntity instanceof Enemy && !(livingEntity instanceof Creeper);
    }
}
