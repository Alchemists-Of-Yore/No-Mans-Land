package com.farcr.nomansland.common.entity.moose;

import com.farcr.nomansland.common.entity.ai.MaintainChaseWithinRange;
import com.farcr.nomansland.common.entity.ai.StartChasingWhenHurt;
import com.farcr.nomansland.common.entity.ai.WarningAttack;
import com.farcr.nomansland.common.registry.entities.NMLSensors;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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

public class MooseAI {

    private static final ImmutableList<SensorType<? extends Sensor<? super Moose>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY,
            NMLSensors.MOOSE_THREATS.get()
    );

    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_ATTACKABLE,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.PATH,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.IS_PANICKING
            );

    public static Brain.Provider<Moose> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    public static Brain<?> makeBrain(Brain<Moose> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initAvoidActivity(brain);
        initFightActivity(brain);

        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static void updateActivity(Moose moose) {
        moose.getBrain().setActiveActivityToFirstValid(ImmutableList.of(
                Activity.AVOID,
                Activity.FIGHT,
                Activity.IDLE
        ));
    }

    private static void initCoreActivity(Brain<Moose> brain) {
        brain.addActivity(Activity.CORE, 0, ImmutableList.of(
                new Swim(0.8F),
                new LookAtTargetSink(45, 90),
                new MoveToTargetSink(),
                new StartChasingWhenHurt<>(),
                StartAttacking.create(MooseAI::findNearestValidAttackTarget)
        ));
    }

    private static void initIdleActivity(Brain<Moose> brain) {
        brain.addActivityWithConditions(
                Activity.IDLE,
                ImmutableList.of(
                        Pair.of(0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
                        Pair.of(1, new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F)),
                        Pair.of(2, new RunOne<>(
                                ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                                ImmutableList.of(
                                        Pair.of(new ShakeOffSaddle(), 1),
                                        Pair.of(RandomStroll.stroll(1.0F), 1),
                                        Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                                        Pair.of(new DoNothing(30, 60), 1)
                                )
                        ))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT),
                        Pair.of(MemoryModuleType.NEAREST_ATTACKABLE, MemoryStatus.VALUE_ABSENT)
                )
        );
    }

    private static void initAvoidActivity(Brain<Moose> brain) {
        brain.addActivityWithConditions(
                Activity.AVOID,
                ImmutableList.of(
                        Pair.of(0, new Stomp()),
                        Pair.of(1, SetWalkTargetAwayFrom.entity(MemoryModuleType.NEAREST_ATTACKABLE, 1.3F, 10, false)),
                        Pair.of(2, WarningAttack.create(80)),
                        Pair.of(3, new Charge())
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.NEAREST_ATTACKABLE, MemoryStatus.VALUE_PRESENT),
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)
                )
        );
    }

    private static void initFightActivity(Brain<Moose> brain) {
        brain.addActivityWithConditions(
                Activity.FIGHT,
                ImmutableList.of(
                        Pair.of(0, new MaintainChaseWithinRange(MemoryModuleType.ATTACK_TARGET, 10)),
                        Pair.of(1, MeleeAttack.create(40)),
                        Pair.of(2, SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(1.7F))
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_PRESENT)
                )
        );
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(Moose moose) {
        Brain<Moose> brain = moose.getBrain();
        Optional<UUID> angerTarget = brain.getMemory(MemoryModuleType.ANGRY_AT);
        NearestVisibleLivingEntities entities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        return angerTarget.flatMap(uuid -> entities.findClosest(entity -> moose.distanceToSqr(entity) < Mth.square(10) && entity.getUUID() == uuid));
    }
}