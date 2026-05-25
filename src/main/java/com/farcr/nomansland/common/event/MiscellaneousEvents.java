package com.farcr.nomansland.common.event;

import com.farcr.nomansland.NMLConfig;
import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.block.torches.ExtinguishableBlockPairing;
import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamingPlayer;
import com.farcr.nomansland.common.entity.bombs.Explosive;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.farcr.nomansland.common.entity.frienderman.Frienderman;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.handler.InvertedBellServerHandler;
import com.farcr.nomansland.common.integration.Mods;
import com.farcr.nomansland.common.networking.buddy.ClientboundBuddyUpdateEffectsPacket;
import com.farcr.nomansland.common.networking.dream.ClientboundDimensionSyncPacket;
import com.farcr.nomansland.common.registry.NMLCriteriaTriggers;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import com.farcr.nomansland.common.registry.items.NMLArmorMaterials;
import com.farcr.nomansland.common.registry.items.NMLDataComponents;
import com.farcr.nomansland.common.registry.items.NMLItems;
import com.farcr.nomansland.common.registry.worldgen.NMLBiomes;
import com.farcr.nomansland.common.registry.worldgen.NMLFeatures;
import com.farcr.nomansland.common.world.densityfunction.LazilyCachedDensityFunctionSeedifier;
import com.farcr.nomansland.common.world.saved_data.RegeneratingPotsData;
import com.farcr.nomansland.common.world.saved_data.WardedSpacesData;
import com.farcr.nomansland.common.worldevent.SunDog;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockGrowFeatureEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Iterator;
import java.util.List;

import static com.farcr.nomansland.common.block.FrostedGrassBlock.SNOWLOGGED;
import static net.minecraft.world.level.block.SnowyDirtBlock.SNOWY;

@EventBusSubscriber(modid = NoMansLand.MODID)
public class MiscellaneousEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        boolean isExtinguishing = stack.getItem().canPerformAction(stack, ItemAbilities.SHOVEL_DOUSE) && NMLConfig.TORCH_EXTINGUISHING.get();
        boolean isLighting = stack.is(NMLTags.FIRESTARTERS) || stack.getItem().canPerformAction(stack, ItemAbilities.FIRESTARTER_LIGHT);
        if (!player.isSpectator() && (isExtinguishing || isLighting)) {
            for (ExtinguishableBlockPairing pair : NMLRegistries.EXTINGUISHABLE_BLOCKS) {
                if (isExtinguishing) { //extinguishing block
                    if (pair.isLitVersion(state)) {
                        level.playSound(player, pos, NMLSounds.TORCH_EXTINGUISH.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
                        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                        level.setBlockAndUpdate(pos, pair.extinguishedBlock().withPropertiesOf(state));
                        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                        event.setCanceled(true);
                        break;
                    }
                } else { //lighting block
                    if (pair.isExtinguishedVersion(state)) {
                        level.playSound(player, pos, stack.is(Items.FLINT_AND_STEEL) ? NMLSounds.TORCH_LIGHT_BY_FLINT_AND_STEEL.get() : NMLSounds.TORCH_LIGHT.get(), SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                        level.setBlockAndUpdate(pos, pair.litBlock().withPropertiesOf(state));
                        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                        event.setCanceled(true);
                        break;
                    }
                }
            }

            // Lighting vanilla candles, candle cakes, and campfires with firestarters
            if (isLighting && !event.isCanceled()
                    && state.hasProperty(BlockStateProperties.LIT)
                    && !state.getValue(BlockStateProperties.LIT)
                    && (state.is(BlockTags.CANDLES) || state.is(BlockTags.CANDLE_CAKES) || state.is(BlockTags.CAMPFIRES))) {
                level.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.4F + 0.8F);
                level.setBlock(pos, state.setValue(BlockStateProperties.LIT, Boolean.TRUE), 11);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
                event.setCanceled(true);
            }
        }

        // Grass Frosting
        if (stack.is(Blocks.SNOW.asItem()) && !player.isSpectator() && state.is(Blocks.SHORT_GRASS) && !Mods.SNOWREALMAGIC.isLoaded()) {
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

    /*
    * To be clear, this exists because Minecraft doesn't actually sync mob effects
    * with the player. it just handles everything related to them on the server
    * including the spawning of particles. the only time an effect is applied is
    * when it is applied to the player itself or when the player is there to witness
    * the effect being applied. for this reason we must manually reapply the effect
    * on respawn or otherwise if the player wasn't there to witness its application
    */
    @SubscribeEvent
    private static void happinessTrackingPacket(PlayerEvent.StartTracking event) {
        if (!(event.getEntity() instanceof ServerPlayer tracker)) return;
        if (event.getTarget() instanceof Buddy buddy && buddy.hasEffect(NMLEffects.HAPPINESS)) {
            MobEffectInstance effectInstance = buddy.getEffect(NMLEffects.HAPPINESS);
            PacketDistributor.sendToPlayer(tracker,
                new ClientboundBuddyUpdateEffectsPacket(buddy.getId(), effectInstance));
        } else if (event.getTarget() instanceof ServerPlayer player && player.hasEffect(NMLEffects.HAPPINESS)) {
            MobEffectInstance effectInstance = player.getEffect(NMLEffects.HAPPINESS);
            tracker.connection.send(
                new ClientboundUpdateMobEffectPacket(player.getId(), effectInstance, false));
        }
    }

    @SubscribeEvent
    private static void playerHappinessAdded(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof ServerPlayer player
            && event.getEffectInstance().getEffect().value() == NMLEffects.HAPPINESS.value()) {
            ((ServerLevel) player.level()).getChunkSource().broadcast(player,
                new ClientboundUpdateMobEffectPacket(player.getId(), event.getEffectInstance(), true));
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
        if (event.getLevel() instanceof ServerLevel serverLevel
                && event.getSpawnType() == MobSpawnType.NATURAL
                && event.getEntity() instanceof Enemy && !event.getEntity().getType().is(NMLTags.WARD_REPELLED_BLACKLIST)) {
            WardedSpacesData wardedSpacesData = WardedSpacesData.get(serverLevel);

            event.setSpawnCancelled(wardedSpacesData.isWarded(serverLevel, event.getEntity().blockPosition()));
        }

        if (event.getLevel() instanceof ServerLevel serverLevel
                && event.getSpawnType() == MobSpawnType.NATURAL
                && event.getEntity() instanceof EnderMan
                && !(event.getEntity() instanceof Frienderman)
                && serverLevel.dimension() == Level.OVERWORLD
                && serverLevel.random.nextFloat() < 0.01f) {
            event.setSpawnCancelled(true);
            Frienderman frienderman = NMLEntities.FRIENDERMAN.get().create(serverLevel);
            if (frienderman != null) {
                frienderman.moveTo(event.getEntity().position());
                frienderman.setYRot(event.getEntity().getYRot());
                serverLevel.addFreshEntity(frienderman);
            }
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        float damage = event.getAmount();

        ItemStack chestplate = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chestplate.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial().is(NMLArmorMaterials.TORTOISE)) {
            Vec3 vec32 = source.getSourcePosition();
            if (vec32 != null) {
                Vec3 vec3 = entity.calculateViewVector(0.0F, entity.getYHeadRot());
                Vec3 vec31 = vec32.vectorTo(entity.position());
                vec31 = new Vec3(vec31.x, 0.0, vec31.z).normalize();
                if (!source.is(DamageTypeTags.BYPASSES_SHIELD) && vec31.dot(vec3) > 0.0) {
                    if (chestplate.get(NMLDataComponents.TIME_WHEN_DISABLED) == null)
                        return;
                    if (entity instanceof Player playerReal)
                        playerReal.awardStat(Stats.ITEM_USED.get(chestplate.getItem()));
                    if (damage >= 3.0F) {
                        int damageToItem = 1 + Mth.floor(damage);
                        InteractionHand interactionhand = entity.getUsedItemHand();
                        if (MiscellaneousEvents.isTortoiseShellDisabled(chestplate, entity))
                            chestplate.hurtAndBreak(damageToItem, entity, EquipmentSlot.CHEST);
                        if (chestplate.isEmpty()) {
                            entity.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                            entity.level().playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.NEUTRAL, 1.0F, 0.2F);
                        }
                    }
                    event.setCanceled(MiscellaneousEvents.isTortoiseShellDisabled(chestplate, entity));
                    MiscellaneousEvents.disableTortoiseShell(source, entity, chestplate);
                    if (event.isCanceled()) {
                        entity.level().playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BLOCK, SoundSource.NEUTRAL, 1.0F, 0.2F);
                    }
                }
            }
        }

        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.is(NMLItems.ANCIENT_BRONZE_MASK)) {
            if (source.getEntity() instanceof Player) {
                int punchCount = helmet.getOrDefault(NMLDataComponents.PUNCH_COUNT, 0);
                if (punchCount >= 4) {
                    entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    entity.spawnAtLocation(helmet.copy());
                } else {
                    helmet.set(NMLDataComponents.PUNCH_COUNT, punchCount + 1);
                    helmet.set(NMLDataComponents.PUNCH_COOLDOWN, 100);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onKnockback(LivingKnockBackEvent event) {
        LivingEntity entity = event.getEntity();
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (stack.getItem() instanceof ArmorItem armorItem && armorItem.getMaterial().is(NMLArmorMaterials.TORTOISE)) {
            if (entity.isCrouching()) {
                event.setStrength(0.0F);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            ItemStack stack = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (stack.is(NMLItems.ANCIENT_BRONZE_MASK)) {
                int punchCooldown = stack.getOrDefault(NMLDataComponents.PUNCH_COOLDOWN, 0);
                if (punchCooldown > 0)
                    stack.set(NMLDataComponents.PUNCH_COOLDOWN, punchCooldown - 1);
                else if (stack.getOrDefault(NMLDataComponents.PUNCH_COUNT, 0) > 0)
                    stack.set(NMLDataComponents.PUNCH_COUNT, 0);

                if (entity instanceof Enemy) entity.addEffect(new MobEffectInstance(NMLEffects.PACIFIED, 200, 0, false, true, true));
            }
        }
    }

    @SubscribeEvent
    public static void onAddAttributeTooltips(AddAttributeTooltipsEvent event) {
        if (event.getStack().is(NMLItems.ANCIENT_BRONZE_MASK)) {
            event.addTooltipLines(Component.translatable("nomansland.tooltip.mask.regeneration").withStyle(ChatFormatting.BLUE));
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        Explosion explosion = event.getExplosion();
        Level level = event.getLevel();

        for (BlockPos pos : event.getAffectedBlocks()) {
            BlockState state = level.getBlockState(pos);

            for (ExtinguishableBlockPairing block : NMLRegistries.EXTINGUISHABLE_BLOCKS) {
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
        if (event.getFeature() != null) {
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
                    if (feature == NMLFeatures.LARGE_AUTUMNAL_OAK)
                        event.setFeature(NMLFeatures.LARGE_AUTUMNAL_OAK_PEAR_05);
                } else if (random.nextBoolean() && fruit >= 6) {
                    if (regularOakFeatures.contains(feature)) event.setFeature(NMLFeatures.OAK_APPLE_05);
                    if (fancyOakFeatures.contains(feature)) event.setFeature(NMLFeatures.FANCY_OAK_APPLE_05);
                    if (feature == NMLFeatures.AUTUMNAL_OAK) event.setFeature(NMLFeatures.AUTUMNAL_OAK_PEAR_05);
                    if (feature == NMLFeatures.LARGE_AUTUMNAL_OAK)
                        event.setFeature(NMLFeatures.LARGE_AUTUMNAL_OAK_PEAR_05);
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
    }

    @SubscribeEvent
    public static void onServerStart(ServerAboutToStartEvent event) {
        NMLBiomes.CAVES_HOLDER = event.getServer().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(NMLBiomes.CAVES);
        NMLBiomes.CAVE_DEPTHS_HOLDER = event.getServer().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(NMLBiomes.CAVE_DEPTHS);
        NMLBiomes.ALCHEMIST_RUINS_HOLDER = event.getServer().registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(NMLBiomes.ALCHEMIST_RUINS);
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppingEvent event) {
        LazilyCachedDensityFunctionSeedifier.clearCache();
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            FriendMoon.getOrDefault(serverLevel).tick();
            RegeneratingPotsData.getOrDefault(serverLevel).tick();
            SunDog.getOrDefault(serverLevel).tick();
            InvertedBellServerHandler.get(serverLevel).tick(serverLevel);
            DreamManager.getOrDefault(serverLevel.getServer())
                .updateFlaggedPlayers();
        } else {
            SunDog.Client.INSTANCE.tick();
            ClientDreamRenderer.getInstance().tick();
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new ClientboundDimensionSyncPacket(serverPlayer.server.levelKeys()));
            SunDog.getOrDefault(serverPlayer.serverLevel()).informPlayerOfSunDogState(serverPlayer);
            FriendMoon.getOrDefault(serverPlayer.serverLevel()).playerSendShadowPacket(serverPlayer);
            DreamManager.getOrDefault(serverPlayer.getServer()).notifyClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            DreamManager manager = DreamManager.getOrDefault(event.getEntity().getServer());
            DreamingPlayer dreamingPlayer = manager.getDreamingPlayer(serverPlayer);
            if (dreamingPlayer != null) dreamingPlayer.discardTether();
        }
    }

    public static void spawnItemParticles(int amount, RandomSource randomSource, Level level, ItemStack itemstack, LivingEntity entity) {
        for (int i = 0; i < amount; i++) {
            Vec3 vec3 = new Vec3(((double) randomSource.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            vec3 = vec3.xRot(-entity.getXRot() * (float) (Math.PI / 180.0));
            vec3 = vec3.yRot(-entity.getYRot() * (float) (Math.PI / 180.0));
            double d0 = (double) (-randomSource.nextFloat()) * 0.6 - 0.3;
            Vec3 vec31 = new Vec3(((double) randomSource.nextFloat() - 0.5) * 0.3, d0, 0.6);
            vec31 = vec31.xRot(-entity.getXRot() * (float) (Math.PI / 180.0));
            vec31 = vec31.yRot(-entity.getYRot() * (float) (Math.PI / 180.0));
            vec31 = vec31.add(entity.getX(), entity.getEyeY(), entity.getZ());
            if (!itemstack.isEmpty())
                ((ServerLevel) level).sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemstack), vec31.x, vec31.y, vec31.z, amount, vec3.x, vec3.y + 0.05, vec3.z, 0.5);
        }
    }

    public static void disableTortoiseShell(DamageSource source, LivingEntity entity, ItemStack stack) {
        if (stack.isEmpty() || !stack.isEmpty() && stack.get(NMLDataComponents.TIME_WHEN_DISABLED) != null && (entity.level().getGameTime() - stack.get(NMLDataComponents.TIME_WHEN_DISABLED)) < 100L)
            return;
        if (source.getEntity() instanceof LivingEntity living) {
            if (living.getItemBySlot(EquipmentSlot.MAINHAND).canDisableShield(living.getItemBySlot(EquipmentSlot.MAINHAND), entity, living)) {
                if (entity instanceof Player player)
                    player.getCooldowns().addCooldown(stack.getItem(), 100);
                stack.set(NMLDataComponents.TIME_WHEN_DISABLED.get(), entity.level().getGameTime());
                MiscellaneousEvents.spawnItemParticles(5, living.getRandom(), living.level(), stack, living);
                entity.level().playSound(null, entity.blockPosition(), SoundEvents.SHIELD_BREAK, SoundSource.NEUTRAL, 1.0F, 0.2F);
            }
        }
    }

    public static boolean isTortoiseShellDisabled(ItemStack stack, Entity entity) {
        if (stack.get(NMLDataComponents.TIME_WHEN_DISABLED) == null)
            return false;
        return (entity.level().getGameTime() - stack.get(NMLDataComponents.TIME_WHEN_DISABLED)) > 100L;
    }
}
