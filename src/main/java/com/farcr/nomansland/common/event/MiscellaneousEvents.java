package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.torches.ExtinguishableBlock;
import com.farcr.nomansland.common.entity.PacifiedAttackGoal;
import com.farcr.nomansland.common.entity.bombs.Explosive;
import com.farcr.nomansland.common.registry.NMLCriteriaTriggers;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.farcr.nomansland.common.registry.worldgen.NMLFeatures;
import com.farcr.nomansland.common.saved_data.WardedSpacesData;
import com.farcr.nomansland.common.world.densityfunction.LazilyCachedDensityFunctionSeedifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.farcr.nomansland.common.block.FrostedGrassBlock.SNOWLOGGED;
import static net.minecraft.world.level.block.SnowyDirtBlock.SNOWY;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = NoMansLand.MODID)
public class MiscellaneousEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        boolean isExtinguishing = stack.is(ItemTags.SHOVELS) && NMLConfig.TORCH_EXTINGUISHING.get();
        boolean isLighting = stack.is(NMLTags.FIRESTARTERS);
        if (!player.isSpectator() && (isExtinguishing || isLighting)) {
            for (ExtinguishableBlock holder : NMLRegistries.EXTINGUISHABLE_BLOCKS) {

                if (isExtinguishing) { //extinguishing block
                    if (state.is(holder.litBlock())) {
                        level.playSound(player, pos, NMLSounds.TORCH_EXTINGUISH.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
                        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                        level.setBlockAndUpdate(pos, holder.extinguishedBlock().withPropertiesOf(state));
                        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                        event.setCanceled(true);
                        break;
                    }
                } else { //lighting block
                    if (state.is(holder.extinguishedBlock())) {
                        level.playSound(player, pos, NMLSounds.TORCH_LIGHT.get(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                        level.setBlockAndUpdate(pos, holder.litBlock().withPropertiesOf(state));
                        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                        event.setCanceled(true);
                        break;
                    }
                }
            }
        }

        // Grass Frosting
        if (stack.is(Blocks.SNOW.asItem()) && !player.isSpectator() && state.is(Blocks.SHORT_GRASS)) {
            level.setBlockAndUpdate(pos, NMLBlocks.FROSTED_GRASS.get().defaultBlockState().setValue(SNOWLOGGED, true));
            stack.consume(1, player);
            level.playSound(player, pos, SoundEvents.SNOW_PLACE, SoundSource.PLAYERS, 1, (level.random.nextFloat() - level.random.nextFloat()) * 0.6F + 1.2F);
            BlockPos posUnder = pos.below();
            BlockState stateUnder = level.getBlockState(posUnder);
            if (stateUnder.getBlock() instanceof SnowyDirtBlock)
                level.setBlockAndUpdate(posUnder, stateUnder.setValue(SNOWY, true));
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
            event.setCanceled(true);
        }

        // Ladder Placement
        if (stack.is(Items.LADDER) && state.is(Blocks.LADDER) && !player.isSpectator() && !player.isCrouching() && !player.isFakePlayer()) {
            Direction ladderFacing = state.getValue(LadderBlock.FACING);
            if (ladderFacing == event.getFace()) {
                BlockPos.MutableBlockPos mutable = pos.below().mutable();
                for (int i = 0; i < NMLConfig.MAX_LADDER_PLACEMENT_LENGTH.get(); i++) {
                    BlockState state2 = level.getBlockState(mutable);
                    if (state2.is(BlockTags.REPLACEABLE)) {
                        if (state.canSurvive(level, mutable)) {
                            SoundType soundtype = state.getSoundType(level, pos, player);
                            level.playSound(player, mutable, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                            stack.consume(1, player);
                            BlockState ladderState = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, ladderFacing);
                            level.setBlockAndUpdate(mutable, ladderState);
                            level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, ladderState));
                            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                            event.setCanceled(true);
                            break;
                        }
                    } else if (!state2.is(Blocks.LADDER)) {
                        break;
                    }
                    mutable.move(Direction.DOWN);
                }
            }
        }

        // Rail Placement
        if (stack.is(ItemTags.RAILS) && state.is(BlockTags.RAILS) && !player.isSpectator() && !player.isCrouching() && !player.isFakePlayer()) {
            Direction playerDir = player.getDirection();
            RailShape railShape = null;
            if (state.getBlock() instanceof BaseRailBlock) {
                railShape = state.getValue(((BaseRailBlock) state.getBlock()).getShapeProperty());
            }
            if (railShape != null) {
                int railCount = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (level.getBlockState(pos.relative(direction)).is(BlockTags.RAILS))
                        railCount++;
                }
                RailShape placedShape;
                BlockPos.MutableBlockPos mutable = pos.mutable();
                // Iterate through the rails to find the end of a connected rail segment
                for (int i = 0; i <= NMLConfig.MAX_RAIL_PLACMENT_LENGTH.get(); i++) {
                    // A load of blockpos + blockstates used lower down
                    BlockPos m = mutable.immutable();
                    BlockPos mBelow = m.below();
                    BlockPos mAbove = m.above();
                    BlockPos mBelow2 = mBelow.below();
                    BlockPos mSlopeBase = mBelow.relative(playerDir.getOpposite());
                    BlockState stateBase = level.getBlockState(m);
                    BlockState stateBelow = level.getBlockState(mBelow);
                    BlockState stateAbove = level.getBlockState(mAbove);
                    BlockState stateBelow2 = level.getBlockState(mBelow2);
                    BlockState stateSlopeBase = level.getBlockState(mSlopeBase);
                    if (stateBase.is(BlockTags.RAILS)) {
                        // Continue along the chain normally
                        RailShape offsetShape = null;
                        if (stateBase.getBlock() instanceof BaseRailBlock) {
                            offsetShape = stateBase.getValue(((BaseRailBlock) stateBase.getBlock()).getShapeProperty());
                        }

                        if (offsetShape == null)
                            break;

                        // The big if chain
                        // Straights are gone because of woke
                        // Curves
                        if (offsetShape == RailShape.NORTH_EAST) {
                            if (playerDir == Direction.SOUTH) playerDir = Direction.EAST;
                            else if (playerDir == Direction.WEST) playerDir = Direction.NORTH;
                        } else if (offsetShape == RailShape.NORTH_WEST) {
                            if (playerDir == Direction.SOUTH) playerDir = Direction.WEST;
                            else if (playerDir == Direction.EAST) playerDir = Direction.NORTH;
                        } else if (offsetShape == RailShape.SOUTH_EAST) {
                            if (playerDir == Direction.NORTH) playerDir = Direction.EAST;
                            else if (playerDir == Direction.WEST) playerDir = Direction.SOUTH;
                        } else if (offsetShape == RailShape.SOUTH_WEST) {
                            if (playerDir == Direction.NORTH) playerDir = Direction.WEST;
                            else if (playerDir == Direction.EAST) playerDir = Direction.SOUTH;
                        }
                        // Ramps - this doesn't handle ramps down since they're at a different y level
                        else if (offsetShape == RailShape.ASCENDING_NORTH) {
                            if (playerDir == Direction.NORTH) mutable.move(Direction.UP);
                            else if (playerDir != Direction.SOUTH) break;
                        } else if (offsetShape == RailShape.ASCENDING_SOUTH) {
                            if (playerDir == Direction.SOUTH) mutable.move(Direction.UP);
                            else if (playerDir != Direction.NORTH) break;
                        } else if (offsetShape == RailShape.ASCENDING_EAST) {
                            if (playerDir == Direction.EAST) mutable.move(Direction.UP);
                            else if (playerDir != Direction.WEST) break;
                        } else if (offsetShape == RailShape.ASCENDING_WEST) {
                            if (playerDir == Direction.WEST) mutable.move(Direction.UP);
                            else if (playerDir != Direction.EAST) break;
                        }
                        // Edge case
                        else if (offsetShape != RailShape.EAST_WEST && offsetShape != RailShape.NORTH_SOUTH) {
                            break;
                        }

                        mutable.move(playerDir);
                    } else if (stateBelow.is(BlockTags.RAILS)) {
                        // If we've got rails below, we've likely got a slope and should continue on the chain there
                        // If it's not connected via a slope there'll be special handling to allow us to chain rails down slopes
                        boolean canGoDown = false;
                        RailShape offsetShape = null;
                        if (stateBelow.getBlock() instanceof BaseRailBlock) {
                            offsetShape = stateBelow.getValue(((BaseRailBlock) stateBelow.getBlock()).getShapeProperty());
                        }

                        if (offsetShape == null) break;

                        if (offsetShape == RailShape.ASCENDING_NORTH) {
                            if (playerDir == Direction.SOUTH) {
                                mutable.move(Direction.DOWN);
                                canGoDown = true;
                            } else if (playerDir != Direction.NORTH) break;
                        } else if (offsetShape == RailShape.ASCENDING_SOUTH) {
                            if (playerDir == Direction.NORTH) {
                                mutable.move(Direction.DOWN);
                                canGoDown = true;
                            } else if (playerDir != Direction.SOUTH) break;
                        } else if (offsetShape == RailShape.ASCENDING_EAST) {
                            if (playerDir == Direction.WEST) {
                                mutable.move(Direction.DOWN);
                                canGoDown = true;
                            } else if (playerDir != Direction.EAST) break;
                        } else if (offsetShape == RailShape.ASCENDING_WEST) {
                            if (playerDir == Direction.EAST) {
                                mutable.move(Direction.DOWN);
                                canGoDown = true;
                            } else if (playerDir != Direction.WEST) break;
                        }

                        if (!canGoDown) {
                            // If we don't have a rail below us that we can follow, we just place a rail straight ahead if possible
                            // This works, somehow
                            // Mostly just copied and simplified from the logic further down the main if chain
                            if (stateBase.is(BlockTags.REPLACEABLE)) {
                                if (placeRail(mutable.immutable(), stack, playerDir, level, player)) {
                                    event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                                    event.setCanceled(true);
                                    break;
                                }
                            } else {
                                // If we can't go down a block and can't place a new rail, give up
                                break;
                            }
                        }
                    } else if (stateBase.isFaceSturdy(level, mutable, Direction.UP, SupportType.RIGID) && stateAbove.is(BlockTags.REPLACEABLE)) {
                        // If we've got a block in front of us with air above, go up the slope
                        if (placeRail(mutable.immutable().above(), stack, playerDir, level, player)) {
                            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                            event.setCanceled(true);
                            break;
                        }
                    } else if (stateBelow2.isFaceSturdy(level, mutable.immutable().below(2), Direction.UP, SupportType.RIGID) && stateSlopeBase.isFaceSturdy(level, mutable.immutable().below().relative(playerDir.getOpposite()), playerDir, SupportType.RIGID) && stateBelow.is(BlockTags.REPLACEABLE)) {
                        // If we have support below us for a slope down, and support below where the slope would go, place it
                        if (placeRail(mutable.immutable().below(), stack, playerDir, level, player)) {
                            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                            event.setCanceled(true);
                            break;
                        }
                    } else if (stateBase.is(BlockTags.REPLACEABLE)) {
                        // If we're at an empty space and nothing else fits, just plop down a rail
                        if (placeRail(mutable.immutable(), stack, playerDir, level, player)) {
                            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                            event.setCanceled(true);
                            break;
                        }
                    } else {
                        // No way to place a rail, give up
                        break;
                    }
                }
            }
        }
    }

    private static boolean placeRail(BlockPos position, ItemStack stack, Direction playerDir, Level level, Player player) {
        BlockState state = ((BlockItem) stack.getItem()).getBlock().defaultBlockState();
        RailShape placedShape = switch (playerDir) {
            case Direction.NORTH, Direction.SOUTH -> RailShape.NORTH_SOUTH;
            case Direction.EAST, Direction.WEST -> RailShape.EAST_WEST;
            default -> null;
        };
        if (state.getBlock() instanceof BaseRailBlock) {
            state = state.setValue(((BaseRailBlock) state.getBlock()).getShapeProperty(), placedShape);
        }
        if (state.canSurvive(level, position)) {
            SoundType soundtype = state.getSoundType(level, position, player);
            level.playSound(player, position, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
            stack.consume(1, player);
            level.setBlockAndUpdate(position, state);
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onFinalizeMobSpawn(FinalizeSpawnEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && event.getSpawnType() == MobSpawnType.NATURAL && event.getEntity() instanceof Monster) {
            WardedSpacesData wardedSpacesData = serverLevel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
                    () -> new WardedSpacesData(new ArrayList<>(), new ArrayList<>()), WardedSpacesData::load), WardedSpacesData.NAME);

            event.setSpawnCancelled(wardedSpacesData.isWarded(event.getEntity().blockPosition()));
        }
    }

    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (event.getEntity() instanceof Mob mob && event.getEffect().value().equals(NMLEffects.PACIFIED.get())) {
            mob.targetSelector.removeAllGoals(goal -> goal instanceof PacifiedAttackGoal);
        }
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (event.getEffectInstance() != null && event.getEntity() instanceof Mob mob && event.getEffectInstance().getEffect().value().equals(NMLEffects.PACIFIED.get())) {
            mob.targetSelector.removeAllGoals(goal -> goal instanceof PacifiedAttackGoal);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        Level level = event.getLevel();

        for (BlockPos pos : event.getAffectedBlocks()) {
            BlockState state = level.getBlockState(pos);

            for (ExtinguishableBlock block : NMLRegistries.EXTINGUISHABLE_BLOCKS) {
                if (state.is(block.litBlock())) {
                    level.gameEvent(explosion.getDirectSourceEntity(), GameEvent.BLOCK_CHANGE, pos);
                    level.setBlock(pos, block.extinguishedBlock().withPropertiesOf(state), 11);
                    level.playSound(null, pos, NMLSounds.TORCH_EXTINGUISH.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    break;
                }
            }

            if (event.getExplosion().getDirectSourceEntity() instanceof Explosive explosive && explosive.getOwner() instanceof ServerPlayer serverPlayer && state.is(Tags.Blocks.ORES)) {
                NMLCriteriaTriggers.MINE_ORE_WITH_EXPLOSIVE.get().trigger(serverPlayer, pos);
            }
        }
    }

    @SubscribeEvent
    public static void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
        if (!NMLConfig.TRAMPLING.get()) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockGrow(BlockGrowFeatureEvent event) {
        ResourceKey<ConfiguredFeature<?, ?>> feature = event.getFeature().getKey();
        List<ResourceKey<ConfiguredFeature<?, ?>>> regularOakFeatures = List.of(
                TreeFeatures.OAK,
                TreeFeatures.OAK_BEES_0002,
                TreeFeatures.OAK_BEES_002,
                TreeFeatures.OAK_BEES_005
        );
        List<ResourceKey<ConfiguredFeature<?, ?>>> fancyOakFeatures = List.of(
                TreeFeatures.FANCY_OAK,
                TreeFeatures.FANCY_OAK_BEES_0002,
                TreeFeatures.FANCY_OAK_BEES_002,
                TreeFeatures.FANCY_OAK_BEES_005
        );
        List<ResourceKey<ConfiguredFeature<?, ?>>> autumnalOakFeatures = List.of(
                NMLFeatures.AUTUMNAL_OAK,
                NMLFeatures.LARGE_AUTUMNAL_OAK
        );
        List<ResourceKey<ConfiguredFeature<?, ?>>> spruceFeatures = List.of(
                TreeFeatures.SPRUCE,
                TreeFeatures.MEGA_SPRUCE,
                TreeFeatures.PINE,
                TreeFeatures.MEGA_PINE
        );
        List<ResourceKey<ConfiguredFeature<?, ?>>> pineFeatures = List.of(
                NMLFeatures.PINE,
                NMLFeatures.LARGE_PINE
        );

        BlockPos pos = event.getPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        LevelAccessor level = event.getLevel();
        int fruit = 0;
        RandomSource random = event.getRandom();

        boolean apple = regularOakFeatures.contains(feature) || fancyOakFeatures.contains(feature);
        boolean pear = autumnalOakFeatures.contains(feature);

        if (apple || pear) {
            Iterator<BlockPos> it = BlockPos.betweenClosedStream(x - 8, y - 12, z - 8, x + 8, y + 12, z + 8).iterator();
            while (it.hasNext()) {
                BlockPos bp = it.next();
                BlockState state = level.getBlockState(bp);
                if (apple && state.is(NMLBlocks.APPLE_FRUIT.block())) fruit++;
                if (pear && state.is(NMLBlocks.PEAR_FRUIT.block())) fruit++;
            }
            if (fruit >= 12) {
                if (regularOakFeatures.contains(feature)) event.setFeature(NMLFeatures.OAK_APPLE_05);
                if (fancyOakFeatures.contains(feature)) event.setFeature(NMLFeatures.FANCY_OAK_APPLE_05);
                if (feature == NMLFeatures.AUTUMNAL_OAK) event.setFeature(NMLFeatures.AUTUMNAL_OAK_PEAR_05);
                if (feature == NMLFeatures.LARGE_AUTUMNAL_OAK) event.setFeature(NMLFeatures.LARGE_AUTUMNAL_OAK_PEAR_05);
            } else if (random.nextBoolean() && fruit >= 6) {
                if (regularOakFeatures.contains(feature)) event.setFeature(NMLFeatures.OAK_APPLE_05);
                if (fancyOakFeatures.contains(feature)) event.setFeature(NMLFeatures.FANCY_OAK_APPLE_05);
                if (feature == NMLFeatures.AUTUMNAL_OAK) event.setFeature(NMLFeatures.AUTUMNAL_OAK_PEAR_05);
                if (feature == NMLFeatures.LARGE_AUTUMNAL_OAK) event.setFeature(NMLFeatures.LARGE_AUTUMNAL_OAK_PEAR_05);
            } else if (fruit > 0) {
                if (regularOakFeatures.contains(feature)) event.setFeature(NMLFeatures.OAK_APPLE_01);
                if (fancyOakFeatures.contains(feature)) event.setFeature(NMLFeatures.FANCY_OAK_APPLE_01);
            }
        }

        if ((spruceFeatures.contains(feature) || pineFeatures.contains(feature)) && level.getLevelData().isRaining() && !level.getBiome(pos).value().warmEnoughToRain(pos)) {
            if (spruceFeatures.contains(feature)) {
                if (feature == TreeFeatures.SPRUCE) event.setFeature(NMLFeatures.FROSTED_SPRUCE);
                if (feature == TreeFeatures.MEGA_SPRUCE) event.setFeature(NMLFeatures.MEGA_FROSTED_SPRUCE);
                if (feature == TreeFeatures.PINE) event.setFeature(NMLFeatures.FROSTED_SPRUCE_ALT);
                if (feature == TreeFeatures.MEGA_PINE) event.setFeature(NMLFeatures.MEGA_FROSTED_SPRUCE_ALT);
            }
            if (pineFeatures.contains(feature)) event.setFeature(NMLFeatures.FROSTED_PINE);
        }
    }

    @SubscribeEvent
    public static void onServerStart(ServerAboutToStartEvent event) {
        NMLBiomes.CAVES_HOLDER = event.getServer().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(NMLBiomes.CAVES);
        NMLBiomes.CAVE_DEPTHS_HOLDER = event.getServer().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(NMLBiomes.CAVE_DEPTHS);
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event) {
        LazilyCachedDensityFunctionSeedifier.clearCache();
    }
}
