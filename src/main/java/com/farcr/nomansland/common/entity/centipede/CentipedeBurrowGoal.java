package com.farcr.nomansland.common.entity.centipede;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CentipedeBurrowGoal extends Goal {
    private enum Phase { SEEKING, SUBMERGING, BURROWED, EMERGING }

    private static final int MAX_BURROW_TICKS = 1200;
    private static final int MAX_SEEK_TICKS = 160;
    private static final double WATCH_RANGE = 14.0;
    private static final double POUNCE_RANGE = 5.0;
    private static final double DRIVE_SPEED = 0.22;
    private static final int MAX_EXPLORE = 140;

    private final Centipede centipede;
    private Phase phase = Phase.SEEKING;
    private List<Vec3> waypoints = new ArrayList<>();
    private int wpIndex;
    private int seekTicks;
    private int burrowedTicks;
    private int emergeTicks;
    private boolean giveUp;
    @Nullable
    private Vec3 approachPoint;
    @Nullable
    private Vec3 exitFacing;
    @Nullable
    private LivingEntity pounceTarget;

    public CentipedeBurrowGoal(Centipede centipede) {
        this.centipede = centipede;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.centipede.getTarget() != null) return false;
        if (!this.centipede.canBurrowNow()) return false;
        if (this.centipede.isShriveling()) return false;
        if (this.centipede.getRandom().nextInt(120) != 0) return false;
        if (findVisiblePrey(WATCH_RANGE) != null) return false;
        return buildPath();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.centipede.isShriveling()) return false;
        if (this.giveUp) return false;
        if (this.phase == Phase.EMERGING) return this.emergeTicks > 0;
        if (this.phase == Phase.SEEKING && this.centipede.getTarget() != null) return false;
        return this.burrowedTicks <= MAX_BURROW_TICKS;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.phase = Phase.SEEKING;
        this.wpIndex = 0;
        this.seekTicks = 0;
        this.burrowedTicks = 0;
        this.giveUp = false;
        if (this.approachPoint != null) {
            this.centipede.getNavigation().moveTo(this.approachPoint.x, this.approachPoint.y, this.approachPoint.z, 1.0);
        }
    }

    @Override
    public void tick() {
        switch (this.phase) {
            case SEEKING -> tickSeeking();
            case SUBMERGING -> tickDriving(Phase.BURROWED);
            case BURROWED -> tickBurrowed();
            case EMERGING -> tickEmerging();
        }
    }

    private void tickSeeking() {
        if (this.approachPoint == null) {
            this.giveUp = true;
            return;
        }
        this.seekTicks++;
        double distSqr = this.centipede.position().distanceToSqr(this.approachPoint);
        if (distSqr <= 2.25) {
            this.centipede.getNavigation().stop();
            this.centipede.setBurrowed(true);
            this.centipede.setBurrowPhysics(true);
            this.phase = Phase.SUBMERGING;
            this.wpIndex = 0;
            return;
        }
        if (this.centipede.getNavigation().isDone()) {
            this.centipede.getNavigation().moveTo(this.approachPoint.x, this.approachPoint.y, this.approachPoint.z, 1.0);
        }
        if (this.seekTicks > MAX_SEEK_TICKS) {
            this.giveUp = true;
        }
    }

    private void tickDriving(Phase onComplete) {
        if (this.wpIndex >= this.waypoints.size()) {
            this.phase = onComplete;
            this.centipede.setDeltaMovement(Vec3.ZERO);
            return;
        }
        Vec3 wp = this.waypoints.get(this.wpIndex);
        Vec3 to = wp.subtract(this.centipede.position());
        double dist = to.length();
        if (dist < 0.35) {
            this.wpIndex++;
            return;
        }
        Vec3 v = to.scale(DRIVE_SPEED / dist);
        this.centipede.setDeltaMovement(v);
        faceTravel(v);
    }

    private void tickBurrowed() {
        this.centipede.getNavigation().stop();
        this.centipede.setDeltaMovement(Vec3.ZERO);
        this.burrowedTicks++;
        if (this.exitFacing != null) {
            faceTravel(this.exitFacing);
        }
        LivingEntity target = this.centipede.getTarget();
        LivingEntity prey = findVisiblePrey(WATCH_RANGE);
        LivingEntity focus = target != null ? target : prey;
        if (focus != null) {
            this.centipede.getLookControl().setLookAt(focus, 90.0F, 90.0F);
            boolean inRange = this.centipede.distanceToSqr(focus) <= POUNCE_RANGE * POUNCE_RANGE;
            if (inRange || target != null) {
                this.pounceTarget = focus;
                this.emergeTicks = 6;
                this.phase = Phase.EMERGING;
            }
        }
    }

    private void tickEmerging() {
        this.emergeTicks--;
        Vec3 dir;
        if (this.pounceTarget != null) {
            Vec3 d = this.pounceTarget.position().subtract(this.centipede.position());
            dir = new Vec3(d.x, 0.0, d.z);
        } else if (this.exitFacing != null) {
            dir = this.exitFacing;
        } else {
            dir = this.centipede.getLookAngle();
        }
        double len = dir.length();
        if (len > 1.0E-4) {
            Vec3 v = dir.scale(0.3 / len);
            this.centipede.setDeltaMovement(v.x, this.centipede.getDeltaMovement().y, v.z);
            faceTravel(v);
        }
        if (this.emergeTicks <= 0) {
            this.centipede.setBurrowPhysics(false);
            this.centipede.setBurrowed(false);
            if (this.pounceTarget != null) {
                double dx = this.pounceTarget.getX() - this.centipede.getX();
                double dz = this.pounceTarget.getZ() - this.centipede.getZ();
                this.centipede.requestLunge(dx, 0.45, dz);
                this.centipede.setTarget(this.pounceTarget);
            }
        }
    }

    private void faceTravel(Vec3 v) {
        if (v.x * v.x + v.z * v.z < 1.0E-6) return;
        float yaw = (float) (Mth.atan2(-v.x, v.z) * Mth.RAD_TO_DEG);
        this.centipede.setYRot(yaw);
        this.centipede.yBodyRot = yaw;
        this.centipede.yHeadRot = yaw;
    }

    private boolean buildPath() {
        double bodyLen = this.centipede.getSegments() * Centipede.SEGMENT_SPACING;
        int required = Mth.clamp((int) Math.ceil(bodyLen) + 1, 3, 14);
        BlockPos origin = this.centipede.blockPosition();
        List<BlockPos> entrances = new ArrayList<>();
        for (BlockPos bp : BlockPos.betweenClosed(origin.offset(-6, -3, -6), origin.offset(6, 3, 6))) {
            if (this.centipede.isBurrowBlock(bp) && airAdjacent(bp)) {
                entrances.add(bp.immutable());
            }
        }
        entrances.sort((a, b) -> Double.compare(a.distSqr(origin), b.distSqr(origin)));
        for (BlockPos entrance : entrances) {
            List<BlockPos> path = new ArrayList<>();
            Set<BlockPos> visited = new HashSet<>();
            if (dfs(entrance, required, path, visited)) {
                materialize(path);
                return true;
            }
        }
        return false;
    }

    private boolean dfs(BlockPos block, int required, List<BlockPos> path, Set<BlockPos> visited) {
        path.add(block);
        visited.add(block);
        if (visited.size() > MAX_EXPLORE) {
            path.remove(path.size() - 1);
            return false;
        }
        if (path.size() >= required && airAdjacent(block)) {
            return true;
        }
        for (Direction dir : Direction.values()) {
            BlockPos nb = block.relative(dir);
            if (!visited.contains(nb) && this.centipede.isBurrowBlock(nb)) {
                if (dfs(nb, required, path, visited)) return true;
            }
        }
        path.remove(path.size() - 1);
        return false;
    }

    private void materialize(List<BlockPos> path) {
        this.waypoints = new ArrayList<>();
        BlockPos entrance = path.get(0);
        BlockPos exit = path.get(path.size() - 1);
        this.approachPoint = airNeighborCenter(entrance, this.centipede.blockPosition());
        for (BlockPos bp : path) {
            this.waypoints.add(Vec3.atCenterOf(bp));
        }
        BlockPos exitAir = airNeighbor(exit);
        if (exitAir != null) {
            Vec3 exitAirCenter = Vec3.atCenterOf(exitAir);
            this.waypoints.add(exitAirCenter);
            this.exitFacing = exitAirCenter.subtract(Vec3.atCenterOf(exit)).normalize();
        } else {
            this.exitFacing = new Vec3(0.0, 1.0, 0.0);
        }
    }

    private boolean airAdjacent(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (this.centipede.isPassableAt(pos.relative(dir))) return true;
        }
        return false;
    }

    @Nullable
    private BlockPos airNeighbor(BlockPos pos) {
        if (this.centipede.isPassableAt(pos.above())) return pos.above().immutable();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (this.centipede.isPassableAt(pos.relative(dir))) return pos.relative(dir).immutable();
        }
        if (this.centipede.isPassableAt(pos.below())) return pos.below().immutable();
        return null;
    }

    private Vec3 airNeighborCenter(BlockPos pos, BlockPos toward) {
        BlockPos best = null;
        double bestD = Double.MAX_VALUE;
        for (Direction dir : Direction.values()) {
            BlockPos nb = pos.relative(dir);
            if (this.centipede.isPassableAt(nb)) {
                double d = nb.distSqr(toward);
                if (d < bestD) {
                    bestD = d;
                    best = nb.immutable();
                }
            }
        }
        return best != null ? Vec3.atCenterOf(best) : Vec3.atCenterOf(pos.above());
    }

    @Nullable
    private LivingEntity findVisiblePrey(double range) {
        Player player = this.centipede.level().getNearestPlayer(this.centipede, range);
        if (player != null && Centipede.canTargetPlayer(player) && this.centipede.hasLineOfSight(player)) {
            return player;
        }
        AABB area = this.centipede.getBoundingBox().inflate(range);
        List<LivingEntity> nearby = this.centipede.level().getEntitiesOfClass(LivingEntity.class, area, Centipede::isPrey);
        LivingEntity closest = null;
        double closestSqr = Double.MAX_VALUE;
        for (LivingEntity entity : nearby) {
            double d = this.centipede.distanceToSqr(entity);
            if (d < closestSqr && this.centipede.hasLineOfSight(entity)) {
                closest = entity;
                closestSqr = d;
            }
        }
        return closest;
    }

    @Override
    public void stop() {
        if (this.centipede.noPhysics) {
            BlockPos hp = this.centipede.blockPosition();
            if (!this.centipede.isPassableAt(hp) && this.approachPoint != null) {
                this.centipede.setPos(this.approachPoint.x, this.approachPoint.y, this.approachPoint.z);
            }
        }
        this.centipede.setBurrowPhysics(false);
        this.centipede.setBurrowed(false);
        this.centipede.resetBurrowCooldown();
        this.phase = Phase.SEEKING;
        this.wpIndex = 0;
        this.burrowedTicks = 0;
        this.emergeTicks = 0;
        this.giveUp = false;
        this.pounceTarget = null;
        this.waypoints = new ArrayList<>();
    }
}
