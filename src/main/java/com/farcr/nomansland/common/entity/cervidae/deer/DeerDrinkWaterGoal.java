package com.farcr.nomansland.common.entity.cervidae.deer;

import net.minecraft.world.entity.ai.goal.Goal;

public abstract class DeerDrinkWaterGoal  extends Goal {

//    private final Deer mob;
//    @Nullable
//    protected Path path;
//    private final Level level;
//    private int drinkAnimationTick;
//
//    public DeerDrinkWaterGoal(Deer mob) {
//        this.mob = mob;
//        this.level = mob.level();
//        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
//    }
//
//    public boolean canUse() {
//        if (mob.getHydration() < 0 && mob.getRandom().nextInt(80) == 0) {
//            Level level = mob.level();
//            Iterable<BlockPos> iterable = BlockPos.betweenClosed(Mth.floor(mob.getX() - 10), Mth.floor(mob.getY() - 10), Mth.floor(mob.getZ() - 10), Mth.floor(mob.getX() + 10), Mth.floor(mob.getY() + 10), Mth.floor(mob.getZ() + 10));
//            BlockPos blockpos = null;
//
//            for (BlockPos blockpos1 : iterable) {
//                if (level.getBlockState(blockpos1).is(Blocks.WATER)) {
//                    for (Direction direction : Direction.Plane.HORIZONTAL) {
//                        if (level.getBlockState(blockpos1.relative(direction)).isPathfindable(PathComputationType.LAND) && mob.getRandom().nextBoolean()) {
//                            blockpos = blockpos1.relative(direction).above();
//                            break;
//                        }
//                    }
//                    if (blockpos != null) {
//                        path = mob.getNavigation().createPath(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 0);
//                        break;
//                    }
//                }
//            }
//        }
//
//        return path != null;
//    }
//
//    public void start() {
//        drinkAnimationTick = this.adjustedTickDelay(mob.getRandom().nextInt(40, 120));
//        level.broadcastEntityEvent(mob, (byte)10);
//        mob.getNavigation().moveTo(path, 1);
//    }
//
//    public boolean canContinueToUse() {
//        return mob.getHydration() < mob.getRandom().nextInt(500, 2000) && drinkAnimationTick > 0;
//    }
//
//    public void tick() {
//        if (!mob.isPathFinding()) {
//            for (Direction direction : Direction.Plane.HORIZONTAL) {
//                if (mob.level().getBlockState(mob.blockPosition().below().relative(direction)).is(Blocks.WATER)) {
//                    drinkAnimationTick = Math.max(0, drinkAnimationTick - 1);
//                    mob.setIsDrinking(drinkAnimationTick != adjustedTickDelay(4));
//                }
//            }
//        } else mob.setIsDrinking(false);
//    }
//
//    public void stop() {
//        mob.setIsDrinking(false);
//    }
//
//    public int getDrinkAnimationTick() {
//        return drinkAnimationTick;
//    }
}
