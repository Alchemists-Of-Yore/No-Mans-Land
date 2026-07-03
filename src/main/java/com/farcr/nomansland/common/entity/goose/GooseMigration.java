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
    private static final int FLYOVER_MIN = 5;
    private static final int FLYOVER_MAX = 9;
    private static final double GATHER_RADIUS = 48.0;
    private static final int LOCAL_GOOSE_CAP = 12;
    private static final double LOCAL_CAP_RADIUS = 64.0;
    private static final int WATER_SEARCH_RADIUS = 28;
    private static final double SPAWN_DISTANCE = 84.0;
    private static final double CLIMB_HEIGHT = 42.0;
    private static final double ARRIVAL_ALTITUDE = 30.0;
    private static final double FLYOVER_ALTITUDE = 46.0;
    private static final double CLUSTER_RADIUS_SQR = 400.0;
    private static final int UNDERGROUND_DEPTH = 10;
    private static final int DEPARTURE_COOLDOWN = 10000;
    private static final int ARRIVAL_COOLDOWN = 10000;
    private static final int FLYOVER_COOLDOWN = 4000;
    private static final int RETRY_DELAY = 800;
    private static final long DUSK_START = 11800L;
    private static final long DUSK_END = 13700L;
    private static final long DAWN_START = 22000L;
    private static final long DAWN_END = 23800L;

    private int airborne;
    private long nextDeparture;
    private long nextArrival;
    private long nextFlyover;

    public static GooseMigration get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(GooseMigration::new, GooseMigration::load), DATA_NAME);
    }

    public static GooseMigration load(CompoundTag tag, HolderLookup.Provider registries) {
        GooseMigration data = new GooseMigration();
        data.airborne = tag.getInt("Airborne");
        data.nextDeparture = tag.getLong("NextDeparture");
        data.nextArrival = tag.getLong("NextArrival");
        data.nextFlyover = tag.getLong("NextFlyover");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Airborne", airborne);
        tag.putLong("NextDeparture", nextDeparture);
        tag.putLong("NextArrival", nextArrival);
        tag.putLong("NextFlyover", nextFlyover);
        return tag;
    }

    public void recordDeparted() {
        airborne++;
        setDirty();
    }

    public void tick(ServerLevel level) {
        if (level.players().isEmpty()) return;
        long time = level.getGameTime();
        long day = level.getDayTime() % 24000L;
        boolean dusk = day >= DUSK_START && day < DUSK_END;
        boolean dawn = day >= DAWN_START && day < DAWN_END;
        boolean storm = level.isThundering();

        if (dawn && !storm && airborne > 0 && time >= nextArrival) {
            if (tryArrival(level)) nextArrival = time + ARRIVAL_COOLDOWN + level.random.nextInt(ARRIVAL_COOLDOWN);
            else nextArrival = time + RETRY_DELAY;
            setDirty();
        }

        if (dusk && !storm && time >= nextDeparture) {
            if (tryDeparture(level)) nextDeparture = time + DEPARTURE_COOLDOWN + level.random.nextInt(DEPARTURE_COOLDOWN);
            else nextDeparture = time + RETRY_DELAY;
            setDirty();
        }

        if (!storm && time >= nextFlyover) {
            boolean twilight = dusk || dawn;
            if ((twilight || level.random.nextInt(4) == 0) && tryFlyover(level)) {
                nextFlyover = time + FLYOVER_COOLDOWN + level.random.nextInt(FLYOVER_COOLDOWN);
            } else {
                nextFlyover = time + RETRY_DELAY + level.random.nextInt(RETRY_DELAY);
            }
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
        float yaw = yawOf(heading);

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

    private boolean tryFlyover(ServerLevel level) {
        ServerPlayer player = pickAudience(level);
        if (player == null) return false;
        if (!level.canSeeSky(player.blockPosition().above(2))) return false;
        if (level.getEntitiesOfClass(Goose.class, player.getBoundingBox().inflate(LOCAL_CAP_RADIUS), Goose::isMigrating).size() >= FLYOVER_MAX) return false;

        double angle = level.random.nextDouble() * Math.PI * 2;
        Vec3 cross = new Vec3(Math.cos(angle), 0, Math.sin(angle));
        double lateral = (level.random.nextDouble() - 0.5) * 48.0;
        Vec3 perp = new Vec3(-cross.z, 0, cross.x).scale(lateral);
        Vec3 base = player.position().add(perp).subtract(cross.scale(SPAWN_DISTANCE));
        int terrain = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(base.x), Mth.floor(base.z));
        double altitude = Math.max(player.getY(), terrain) + FLYOVER_ALTITUDE + level.random.nextInt(22);
        if (!level.isLoaded(BlockPos.containing(base.x, altitude, base.z))) return false;

        float yaw = yawOf(cross);
        int size = FLYOVER_MIN + level.random.nextInt(FLYOVER_MAX - FLYOVER_MIN + 1);
        Goose leader = null;
        for (int index = 0; index < size; index++) {
            Goose goose = NMLEntities.GOOSE.get().create(level);
            if (goose == null) break;
            Vec3 offset = index == 0 ? Vec3.ZERO : GooseMigrationBehavior.formationOffset(index, cross);
            Vec3 position = new Vec3(base.x, altitude, base.z).add(offset);
            goose.moveTo(position.x, position.y, position.z, yaw, 0);
            goose.startTransit(index == 0 ? null : leader, index, cross);
            level.addFreshEntity(goose);
            if (index == 0) leader = goose;
        }
        return leader != null;
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

    private static float yawOf(Vec3 heading) {
        return (float) (Mth.atan2(heading.z, heading.x) * (180.0 / Math.PI)) - 90.0F;
    }
}
