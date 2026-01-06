package com.farcr.nomansland.common.entity.cervidae;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ShedAntlersGoal extends Goal {

    private final PathfinderMob mob;
    private final IAntlers antlerData;

    @Nullable
    protected Path path;
    private final Level level;
    private int shedAnimationTick;

    public ShedAntlersGoal(PathfinderMob mob) {
        this.mob = mob;
        this.antlerData = (IAntlers) mob;
        level = mob.level();
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    public boolean canUse() {
        if (shedAnimationTick == 0) {
            if (antlerData.shouldShedAntlers()) {
                if (mob.level().getGameTime() % 80 > 0L) {
                    return false;
                }
                var nearbyBlocks = getNearbyBlocks(10, 6);
                BlockPos sheddingSpot = null;

                for (BlockPos pos : nearbyBlocks) {
                    if (level.getBlockState(pos).is(BlockTags.LOGS)) {
                        int orderOffset = mob.getRandom().nextInt(4);
                        for (int i = 0; i < 4; i++) {
                            int index = (i + orderOffset) % 4;
                            var direction = Direction.from2DDataValue(index);
                            var grassPos = pos.below(2).relative(direction);
                            var hopefullyGrass = level.getBlockState(grassPos);
                            if (hopefullyGrass.isPathfindable(PathComputationType.LAND)) {
                                sheddingSpot = pos.relative(direction).above();
                                break;
                            }
                        }
                    }
                }
                if (sheddingSpot != null) {
                    path = mob.getNavigation().createPath(sheddingSpot, 0);
                }
            }
        }
        
        return path != null;
    }

    public void start() {
        shedAnimationTick = adjustedTickDelay(mob.getRandom().nextInt(40, 120));
        mob.getNavigation().moveTo(path, 1);
//        Minecraft.getInstance().player.displayClientMessage(Component.literal(mob.getName().getString() + " is looking for logs to shed their pretty little antlers at :3"), false);
    }

    public boolean canContinueToUse() {
        if (path != null && !path.isDone()) {
            return true;
        }
        return antlerData.hasAntlers();
    }

    public void tick() {
        if (mob.isPathFinding()) {
            return;
        }

        var nearbyBlocks = getNearbyBlocks(1);
        boolean hasNearbyLog = false;
        for (BlockPos pos : nearbyBlocks) {
            if (level.getBlockState(pos).is(BlockTags.LOGS)) {
                hasNearbyLog = true;
                break;
            }
        }
        if (hasNearbyLog) {
            shedAnimationTick = Math.max(0, shedAnimationTick - 1);
            if (shedAnimationTick == adjustedTickDelay(4)) {
                antlerData.removeAntlers(mob.getRandom());
                antlerData.onShedAntlers();
            }
        }
    }

    public Iterable<BlockPos> getNearbyBlocks(int range) {
        return getNearbyBlocks(range, range);
    }
    public Iterable<BlockPos> getNearbyBlocks(int horizontal, int vertical) {
        return BlockPos.betweenClosed(Mth.floor(mob.getX() - horizontal), Mth.floor(mob.getY() - vertical), Mth.floor(mob.getZ() - horizontal), Mth.ceil(mob.getX() + horizontal), Mth.ceil(mob.getY() + vertical), Mth.ceil(mob.getZ() + horizontal));
    }

    public void stop() {
        shedAnimationTick = 0;
        path = null;
        mob.getNavigation().stop();
    }

    public int getShedAnimationTick() {
        return shedAnimationTick;
    }
}
