package com.farcr.nomansland.common.entity.goose;

import com.farcr.nomansland.NoMansLand;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;
import java.util.Set;

public class GooseAI {

    private static final ImmutableList<SensorType<? extends Sensor<? super Goose>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY,
            SensorType.NEAREST_PLAYERS,
            SensorType.IS_IN_WATER
    );

    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.IS_PANICKING,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.IS_IN_WATER
    );

    public static Brain.Provider<Goose> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<Goose> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFloatingActivity(brain);
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Goose> brain) {
        brain.addActivity(
                Activity.CORE,
                0,
                ImmutableList.of(
                        new Swim(0.8F),
                        new MoveToTargetSink(500, 700)
                )
        );
    }

    private static void initIdleActivity(Brain<Goose> brain) {
        brain.addActivity(
                Activity.IDLE,
                ImmutableList.of(
                        Pair.of(0, new LookAtTargetSink(45, 90)),
                        Pair.of(
                                1,
                                new RunOne<>(
                                        ImmutableList.of(
                                                Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 2),
                                                Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 6.0F), 1),
                                                Pair.of(RandomStroll.stroll(1.0F), 1),
                                                Pair.of(new DoNothing(5, 20), 2)
                                        )
                                )
                        )
                )
        );
    }

    private static void initFloatingActivity(Brain<Goose> brain) {
        brain.addActivityWithConditions(
                Activity.SWIM,
                ImmutableList.of(
                        Pair.of(0, new Drinking(100, 1000))
                ),
                Set.of(Pair.of(MemoryModuleType.IS_IN_WATER, MemoryStatus.VALUE_PRESENT))
        );
    }

    public static void updateActivity(Goose Goose) {
        Goose.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.SWIM, Activity.IDLE));
    }

    private static class Drinking extends Behavior<Goose> {

        Drinking(int minDuration, int maxDuration) {
            super(Map.of(MemoryModuleType.IS_PANICKING, MemoryStatus.VALUE_ABSENT), minDuration, maxDuration);
        }

        protected boolean checkExtraStartConditions(ServerLevel level, Goose goose) {
            return true;
        }

        protected boolean canStillUse(ServerLevel level, Goose goose, long gameTime) {
            return true;
        }

        protected void start(ServerLevel level, Goose goose, long gameTime) {
            NoMansLand.LOGGER.debug("drinking");
        }

        protected void stop(ServerLevel level, Goose goose, long gameTime) {
        }
    }
}
