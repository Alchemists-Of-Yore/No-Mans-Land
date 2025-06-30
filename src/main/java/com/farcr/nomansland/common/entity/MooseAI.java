package com.farcr.nomansland.common.entity;

import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.entities.NMLMemoryModules;
import com.farcr.nomansland.common.registry.entities.NMLSensors;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.types.Func;
import com.mojang.datafixers.util.Pair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class MooseAI {
    private static final UniformInt ADULT_FOLLOW_RANGE = UniformInt.of(5, 16);
    private static final UniformInt TIME_BETWEEN_ATTACKS = UniformInt.of(150, 200);
    private static final ImmutableList<SensorType<? extends Sensor<? super Moose>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.HURT_BY,
            NMLSensors.MOOSE_TEMPTATIONS.get(),
            SensorType.NEAREST_ADULT
    );
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.IS_PANICKING,
            MemoryModuleType.HURT_BY,
            MemoryModuleType.HURT_BY_ENTITY,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.TEMPTING_PLAYER,
            MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
            MemoryModuleType.IS_TEMPTED,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.NEAREST_VISIBLE_ADULT,
            MemoryModuleType.DANGER_DETECTED_RECENTLY,
            NMLMemoryModules.FIGHT_COOLDOWN_TICKS.get()
    );
    public static Brain.Provider<Moose> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static void initMemories(Moose moose, RandomSource random) {
        moose.getBrain().setMemory(NMLMemoryModules.FIGHT_COOLDOWN_TICKS.get(), TIME_BETWEEN_ATTACKS.sample(random));
    }

    protected static Brain<?> makeBrain(Brain<Moose> brain) {
        initCoreActivity(brain);
        initIdleActivity(brain);
        initFightActivity(brain);
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.useDefaultActivity();
        return brain;
    }

    private static void initCoreActivity(Brain<Moose> brain) {
        brain.addActivity(
                Activity.CORE,
                0,
                ImmutableList.of(
                        new AnimalPanic<>(2.0F),
                        new LookAtTargetSink(45, 90),
                        new MoveToTargetSink(),
                        new CountDownCooldownTicks(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS),
                        new CountDownCooldownTicks(NMLMemoryModules.FIGHT_COOLDOWN_TICKS.get())
                )
        );
    }

    private static void initIdleActivity(Brain<Moose> brain) {
        brain.addActivity(
                Activity.IDLE,
                ImmutableList.of(
                        Pair.of(0, SetEntityLookTargetSometimes.create(EntityType.PLAYER, 6.0F, UniformInt.of(30, 60))),
                        Pair.of(1, new AnimalMakeLove(NMLEntities.MOOSE.get(), 1.0F, 1)),
                        Pair.of(
                                2,
                                new RunOne<>(
                                        ImmutableList.of(
                                                Pair.of(new FollowTemptation(entity -> 1.25F, entity -> entity.isBaby() ? 1.0 : 2.0), 1),
                                                Pair.of(BabyFollowAdult.create(ADULT_FOLLOW_RANGE, 1.25F), 1)
                                        )
                                )
                        ),
                        Pair.of(3, new RandomLookAround(UniformInt.of(150, 250), 30.0F, 0.0F, 0.0F)),
                        Pair.of(
                                4,
                                new RunOne<>(
                                        ImmutableMap.of(MemoryModuleType.WALK_TARGET, MemoryStatus.VALUE_ABSENT),
                                        ImmutableList.of(
                                                Pair.of(RandomStroll.stroll(1.0F), 1),
                                                Pair.of(SetWalkTargetFromLookTarget.create(1.0F, 3), 1),
                                                Pair.of(new DoNothing(30, 60), 1)
                                        )
                                )
                        )
                )
        );
    }

    private static void initFightActivity(Brain<Moose> brain) {
        brain.addActivityWithConditions(
                Activity.FIGHT,
                ImmutableList.of(
                        Pair.of(
                                0,
                                new Stomp(moose -> TIME_BETWEEN_ATTACKS)
                        )
                ),
                ImmutableSet.of(
                        Pair.of(NMLMemoryModules.FIGHT_COOLDOWN_TICKS.get(), MemoryStatus.VALUE_ABSENT)
                )
        );
    }

    public static void updateActivity(Moose moose) {
        moose.getBrain().setActiveActivityToFirstValid(ImmutableList.of(Activity.FIGHT, Activity.IDLE));
    }

    public static Predicate<ItemStack> getTemptations() {
        return item -> item.is(NMLTags.MOOSE_FOOD);
    }

    public static class Stomp extends Behavior<Moose> {
        private final Function<Moose, UniformInt> getTimeBetweenAttacks;

        public Stomp(Function<Moose, UniformInt> getTimeBetweenAttacks) {
            super(Map.of());
            this.getTimeBetweenAttacks = getTimeBetweenAttacks;
        }

        protected boolean checkExtraStartConditions(ServerLevel level, Moose owner) {
            if (!isPlayerTooClose(owner)) {
                return false;
            }
            return true;
        }

        protected void tick(ServerLevel level, Moose owner, long gameTime) {
            super.tick(level, owner, gameTime);
        }

        protected boolean canStillUse(ServerLevel level, Moose entity, long gameTime) {
            return !entity.shouldEndStomping();
        }

        protected void start(ServerLevel level, Moose entity, long gameTime) {
            entity.beginStomp();
        }

        protected void stop(ServerLevel level, Moose entity, long gameTime) {
            if (entity.shouldEndStomping()) {
                entity.endStomp();
            }
            entity.getBrain().setMemory(NMLMemoryModules.FIGHT_COOLDOWN_TICKS.get(), ((UniformInt) this.getTimeBetweenAttacks.apply(entity)).sample(level.random));
        }

        private boolean isPlayerTooClose(Moose moose) {
            Optional<List<LivingEntity>> nearbyEntities = moose.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
            if (!nearbyEntities.isPresent())
            {
                return false;
            }
            for (LivingEntity entity : nearbyEntities.get())
            {
                if (!(entity instanceof Moose))
                {
                    if (moose.getPosition(0.0f).distanceTo(entity.getPosition(0.0f)) < 5.0f) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
