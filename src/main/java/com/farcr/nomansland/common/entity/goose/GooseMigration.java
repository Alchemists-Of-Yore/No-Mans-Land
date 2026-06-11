package com.farcr.nomansland.common.entity.goose;

import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GooseMigration extends SavedData {
    private static final String DATA_NAME = "goose_migration";
    private static final int MIN_FLOCK = 3;
    private static final int MAX_FLOCK = 5;
    private static final double GATHER_RADIUS = 48.0;
    private static final int LOCAL_GOOSE_CAP = 10;
    private static final double LOCAL_CAP_RADIUS = 64.0;
    private static final int DEPARTURE_INTERVAL = 12000;
    private static final int ARRIVAL_INTERVAL = 6000;
    private static final int WATER_SEARCH_RADIUS = 24;
    private static final double SPAWN_DISTANCE = 56.0;
    private static final double CLIMB_HEIGHT = 40.0;
    private static final double ARRIVAL_ALTITUDE = 32.0;
    private static final double CLUSTER_RADIUS_SQR = 400.0;
    private static final int UNDERGROUND_DEPTH = 12;

    private int airborne;
    private long nextDeparture;
    private long nextArrival;

    public static GooseMigration get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(GooseMigration::new, GooseMigration::load), DATA_NAME);
    }

    public static GooseMigration load(CompoundTag tag, HolderLookup.Provider registries) {
        GooseMigration data = new GooseMigration();
        data.airborne = tag.getInt("Airborne");
        data.nextDeparture = tag.getLong("NextDeparture");
        data.nextArrival = tag.getLong("NextArrival");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Airborne", airborne);
        tag.putLong("NextDeparture", nextDeparture);
        tag.putLong("NextArrival", nextArrival);
        return tag;
    }

    public void recordDeparted() {
        airborne++;
        setDirty();
    }

    public void tick(ServerLevel level) {
        if (level.players().isEmpty()) return;
        long time = level.getGameTime();

        if (time >= nextArrival && airborne > 0 && tryArrival(level)) {
            nextArrival = time + ARRIVAL_INTERVAL + level.random.nextInt(ARRIVAL_INTERVAL);
            setDirty();
        }

        if (time >= nextDeparture && tryDeparture(level)) {
            nextDeparture = time + DEPARTURE_INTERVAL + level.random.nextInt(DEPARTURE_INTERVAL);
            setDirty();
        }
    }

    private boolean tryDeparture(ServerLevel level) {
        ServerPlayer player = pickAudience(level);
        if (player == null) return false;
        List<Goose> nearby = level.getEntitiesOfClass(Goose.class, player.getBoundingBox().inflate(GATHER_RADIUS), GooseMigration::canDepart);
        if (nearby.size() < MIN_FLOCK) return false;
        List<Goose> flock = clusterFlock(nearby);
        if (flock == null) return false;

        Goose leader = flock.get(0);
        double angle = level.random.nextDouble() * Math.PI * 2;
        Vec3 heading = new Vec3(Math.cos(angle), 0, Math.sin(angle));
        double ceiling = level.getHeight(Heightmap.Types.MOTION_BLOCKING, leader.getBlockX(), leader.getBlockZ())
                + CLIMB_HEIGHT + level.random.nextInt(10);
        for (int index = 0; index < flock.size(); index++) {
            Goose goose = flock.get(index);
            goose.startMigration(index == 0 ? null : leader, index, heading, ceiling);
        }
        return true;
    }

    @Nullable
    private static List<Goose> clusterFlock(List<Goose> nearby) {
        for (Goose candidate : nearby) {
            List<Goose> cluster = new ArrayList<>();
            for (Goose other : nearby) {
                if (other.distanceToSqr(candidate) <= CLUSTER_RADIUS_SQR) cluster.add(other);
            }
            if (cluster.size() >= MIN_FLOCK) {
                cluster.sort(Comparator.comparingDouble(goose -> goose.distanceToSqr(candidate)));
                return cluster.subList(0, Math.min(cluster.size(), MAX_FLOCK));
            }
        }
        return null;
    }

    private boolean tryArrival(ServerLevel level) {
        ServerPlayer player = pickAudience(level);
        if (player == null) return false;
        if (level.getEntitiesOfClass(Goose.class, player.getBoundingBox().inflate(LOCAL_CAP_RADIUS)).size() >= LOCAL_GOOSE_CAP) return false;
        BlockPos landing = findLanding(level, player);
        if (landing == null) return false;

        int size = Math.min(airborne, MIN_FLOCK + level.random.nextInt(MAX_FLOCK - MIN_FLOCK + 1));
        double angle = level.random.nextDouble() * Math.PI * 2;
        Vec3 fromDirection = new Vec3(Math.cos(angle), 0, Math.sin(angle));
        Vec3 spawnBase = Vec3.atBottomCenterOf(landing).add(fromDirection.scale(SPAWN_DISTANCE));
        double spawnY = Math.max(landing.getY(),
                level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(spawnBase.x), Mth.floor(spawnBase.z))) + ARRIVAL_ALTITUDE;
        if (!level.isLoaded(BlockPos.containing(spawnBase.x, spawnY, spawnBase.z))) return false;
        Vec3 heading = fromDirection.scale(-1);
        float yaw = (float) (Mth.atan2(heading.z, heading.x) * (180.0 / Math.PI)) - 90.0F;

        Goose leader = null;
        int spawned = 0;
        for (int index = 0; index < size; index++) {
            Goose goose = NMLEntities.GOOSE.get().create(level);
            if (goose == null) break;
            Vec3 offset = index == 0 ? Vec3.ZERO : GooseMigrationBehavior.formationOffset(index, heading);
            Vec3 position = new Vec3(spawnBase.x, spawnY, spawnBase.z).add(offset);
            goose.moveTo(position.x, position.y, position.z, yaw, 0);
            BlockPos spot = index == 0 ? landing
                    : level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                            landing.offset(level.random.nextInt(5) - 2, 0, level.random.nextInt(5) - 2));
            goose.startArrival(index == 0 ? null : leader, index, heading, spot);
            level.addFreshEntity(goose);
            if (index == 0) leader = goose;
            spawned++;
        }
        if (spawned == 0) return false;
        airborne -= spawned;
        setDirty();
        return true;
    }

    @Nullable
    private static ServerPlayer pickAudience(ServerLevel level) {
        List<ServerPlayer> eligible = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isSpectator()) continue;
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, player.getBlockX(), player.getBlockZ());
            if (player.getY() < surfaceY - UNDERGROUND_DEPTH) continue;
            eligible.add(player);
        }
        return eligible.isEmpty() ? null : eligible.get(level.random.nextInt(eligible.size()));
    }

    private static boolean canDepart(Goose goose) {
        return goose.isAlive() && !goose.isBaby() && !goose.isCarrying() && !goose.isStealing()
                && !goose.isFlying() && !goose.isMigrating() && (goose.onGround() || goose.isInWater())
                && !goose.isInLove() && !goose.isLeashed() && !goose.isPassenger()
                && !goose.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && !goose.getBrain().hasMemoryValue(MemoryModuleType.AVOID_TARGET);
    }

    @Nullable
    private static BlockPos findLanding(ServerLevel level, ServerPlayer player) {
        BlockPos origin = player.blockPosition();
        BlockPos water = null;
        double best = Double.MAX_VALUE;
        for (int dx = -WATER_SEARCH_RADIUS; dx <= WATER_SEARCH_RADIUS; dx++) {
            for (int dz = -WATER_SEARCH_RADIUS; dz <= WATER_SEARCH_RADIUS; dz++) {
                int x = origin.getX() + dx;
                int z = origin.getZ() + dz;
                BlockPos surface = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1, z);
                if (!level.getFluidState(surface).is(FluidTags.WATER)) continue;
                double distance = surface.distSqr(origin);
                if (distance < best) {
                    best = distance;
                    water = surface.above();
                }
            }
        }
        if (water != null) return water;

        double angle = level.random.nextDouble() * Math.PI * 2;
        int distance = 12 + level.random.nextInt(10);
        int x = origin.getX() + (int) (Math.cos(angle) * distance);
        int z = origin.getZ() + (int) (Math.sin(angle) * distance);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        BlockPos ground = new BlockPos(x, y, z);
        return level.getBlockState(ground.below()).isAir() ? null : ground;
    }
}
