package com.farcr.nomansland.common.entity.goose;

import com.farcr.nomansland.common.registry.entities.NMLEntities;
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
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Set;

public class GooseAI {

    private static final ImmutableList<SensorType<? extends Sensor<? super Goose>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY,
            SensorType.NEAREST_PLAYERS,
            SensorType.IS_IN_WATER,
            SensorType.NEAREST_ADULT
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
            MemoryModuleType.AVOID_TARGET,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.NEAREST_VISIBLE_ADULT,
            MemoryModuleType.IS_PANICKING
    );

    public static Brain.Provider<Goose> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<Goose> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(brain);
        initAvoidActivity(brain);
        brain.addActivity(Activity.RIDE, 0, ImmutableList.of());
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static void updateActivity(Goose goose) {
        if (goose.isFlying()) {
            goose.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.RIDE));
        } else {
            goose.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.AVOID, Activity.FIGHT, Activity.IDLE));
        }
    }

    private static void initCoreActivity(Brain<Goose> brain) {
        brain.addActivity(
                Activity.CORE,
                0,
                ImmutableList.of(
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new GooseCoreBehavior(),
                        new GooseMigrationBehavior(),
                        new GooseFlightBehavior(),
                        new GooseCarryBehavior(),
                        new GoosePeaceOfferingBehavior()
                )
        );
    }

    private static void initIdleActivity(Brain<Goose> brain) {
        brain.addActivity(
                Activity.IDLE,
                ImmutableList.of(
                        Pair.of(0, new AnimalMakeLove(NMLEntities.GOOSE.get(), 1, 1)),
                        Pair.of(1, BabyFollowAdult.create(UniformInt.of(5, 16), 1.25F)),
                        Pair.of(2, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
                        Pair.of(3, new GooseStealBehavior()),
                        Pair.of(4, new GooseHonkAtBehavior()),
                        Pair.of(5, new GooseDrinkBehavior()),
                        Pair.of(6, new GooseSocializeBehavior()),
                        Pair.of(7, new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F)),
                        Pair.of(8, new RunOne<>(
                                ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                                ImmutableList.of(
                                        Pair.of(RandomStroll.stroll(1.0F), 1),
                                        Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                                        Pair.of(new DoNothing(30, 60), 1)
                                )
                        ))
                )
        );
    }

    private static void initFightActivity(Brain<Goose> brain) {
        brain.addActivityWithConditions(
                Activity.FIGHT,
                ImmutableList.of(
                        Pair.of(0, new GooseAttackBehavior())
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
                        Pair.of(0, new GooseThreatenBehavior())
                ),
                ImmutableSet.of(
                        Pair.of(MemoryModuleType.AVOID_TARGET, MemoryStatus.VALUE_PRESENT),
                        Pair.of(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)
                )
        );
    }

    // Anything that wanders within a few blocks — a passing player or a lurking monster — earns a wary standoff.
    public static boolean isThreat(LivingEntity entity) {
        return (entity instanceof Monster || entity instanceof Player) && entity.canBeSeenAsEnemy();
    }
}
