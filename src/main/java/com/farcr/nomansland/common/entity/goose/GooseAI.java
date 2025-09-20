package com.farcr.nomansland.common.entity.goose;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Set;

public class GooseAI {

    private static final ImmutableList<SensorType<? extends Sensor<? super Goose>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY,
            SensorType.NEAREST_PLAYERS,
            SensorType.IS_IN_WATER
    );

    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.PATH,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.NEAREST_PLAYERS,
            MemoryModuleType.IS_IN_WATER,
            MemoryModuleType.AVOID_TARGET
    );

    public static Brain.Provider<Goose> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<Goose> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(brain);
        initAvoidActivity(brain);
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static void updateActivity(Goose goose) {
        goose.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.AVOID, Activity.FIGHT, Activity.IDLE));
    }

    private static void initCoreActivity(Brain<Goose> brain) {
        brain.addActivity(
                Activity.CORE,
                0,
                ImmutableList.of(
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new GooseCoreBehavior()
                )
        );
    }

    private static void initIdleActivity(Brain<Goose> brain) {
        brain.addActivityWithConditions(
                Activity.IDLE,
                ImmutableList.of(
                        Pair.of(0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
                        Pair.of(1, new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F)),
                        Pair.of(2, new RunOne<>(
                                ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                                ImmutableList.of(
                                        Pair.of(RandomStroll.stroll(1.0F), 1),
                                        Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                                        Pair.of(new DoNothing(30, 60), 1)
                                )
                        ))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)
                )
        );
    }

    private static void initFightActivity(Brain<Goose> brain) {
        brain.addActivityWithConditions(
                Activity.FIGHT,
                ImmutableList.of(
                        Pair.of(0, MeleeAttack.create(15)),
                        Pair.of(1, SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.3F))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
                )
        );
    }

    private static void initAvoidActivity(Brain<Goose> brain) {
        brain.addActivityWithConditions(
                Activity.AVOID,
                ImmutableList.of(
                        Pair.of(0, SetWalkTargetAwayFrom.entity(MemoryModuleType.AVOID_TARGET, 1.3F, 8, true))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.AVOID_TARGET, MemoryStatus.VALUE_PRESENT)
                )
        );
    }

    public static boolean isThreat(LivingEntity entity) {
        return entity instanceof Enemy || entity instanceof Player;
    }
}
