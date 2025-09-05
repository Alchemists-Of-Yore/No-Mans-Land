package com.farcr.nomansland.common.entity.goose;

import com.farcr.nomansland.common.entity.ai.MaintainChaseWithinRange;
import com.farcr.nomansland.common.entity.ai.StartChasingWhenHurt;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
            MemoryModuleType.IS_IN_WATER
    );

    public static Brain.Provider<Goose> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<Goose> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(brain);
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static void updateActivity(Goose Goose) {
        Goose.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }

    private static void initCoreActivity(Brain<Goose> brain) {
        brain.addActivity(
                Activity.CORE,
                0,
                ImmutableList.of(
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new StartChasingWhenHurt<>(),
                        StartAttacking.create(GooseAI::findNearestValidAttackTarget)
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
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)
                )
        );
    }

    private static void initFightActivity(Brain<Goose> brain) {
        brain.addActivityWithConditions(
                Activity.FIGHT,
                ImmutableList.of(
                        Pair.of(0, new MaintainChaseWithinRange(MemoryModuleType.ATTACK_TARGET, 20)),
                        Pair.of(1, new Intimidate()),
                        Pair.of(2, new Peck())
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
                )
        );
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Goose goose) {
        Brain<Goose> brain = goose.getBrain();
        Optional<UUID> angerTarget = brain.getMemory(MemoryModuleType.ANGRY_AT);
        NearestVisibleLivingEntities entities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        return angerTarget.flatMap(uuid -> entities.findClosest(entity -> goose.distanceToSqr(entity) < Mth.square(10) && entity.getUUID() == uuid));
    }
}
