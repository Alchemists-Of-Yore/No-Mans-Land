package com.farcr.nomansland.common.entity.buddy;

import com.farcr.nomansland.common.entity.goose.Goose;
import com.farcr.nomansland.common.entity.goose.GooseCoreBehavior;
import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.LookAtTargetSink;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Set;

public class BuddyAI {

    private static final ImmutableList<SensorType<? extends Sensor<? super Buddy>>> SENSOR_TYPES = ImmutableList.of(
        SensorType.NEAREST_LIVING_ENTITIES,
        SensorType.HURT_BY,
        SensorType.NEAREST_PLAYERS,
        SensorType.IS_IN_WATER
    );

    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.NEAREST_VISIBLE_PLAYER,
        MemoryModuleType.HURT_BY_ENTITY,
        MemoryModuleType.LOOK_TARGET,
        MemoryModuleType.WALK_TARGET,
        MemoryModuleType.PATH,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
        MemoryModuleType.NEAREST_PLAYERS,
        MemoryModuleType.IS_IN_WATER,
        MemoryModuleType.NEAREST_VISIBLE_ADULT
    );

    public static Brain.Provider<Buddy> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<?> makeBrain(Brain<Buddy> brain) {
        brain.addActivity(
            Activity.CORE,
            0,
            ImmutableList.of(
                new BuddyBehavior()
            )
        );

        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    public static void updateActivity(Buddy buddy) {

    }
}
