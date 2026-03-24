package com.farcr.nomansland.common.block.pots;

import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.world.saved_data.RegeneratingPotsData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;

import javax.annotation.Nullable;
import java.util.List;

public class PotBlock extends BaseEntityBlock implements SimpleWaterloggedBlock, Fallable {
    public static final MapCodec<PotBlock> CODEC = RecordCodecBuilder.mapCodec(
            (instance) -> instance.group(
                    PotSize.CODEC.fieldOf("size").forGetter(p -> p.size),
                    propertiesCodec()
            ).apply(instance, PotBlock::new)
    );

    private static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final BooleanProperty BRITTLE = BooleanProperty.create("brittle");
    private static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    private final PotSize size;

    public PotBlock(PotSize size, Properties properties) {
        super(properties);
        this.size = size;
        registerDefaultState(stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(BRITTLE, false).setValue(POWERED, false));
    }

    public MapCodec<PotBlock> codec() {
        return CODEC;
    }

    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        level.scheduleTick(pos, this, this.getDelayAfterPlace());

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, false), 2);
            level.updateNeighborsAt(pos, this);
        }

        if (isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            FallingBlockEntity.fall(level, pos, state);
        }
    }

    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    protected int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getSignal(blockAccess, pos, side);
    }

    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED) ? 15 : 0;
    }

    private void startSignal(Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            BlockState state = level.getBlockState(pos);
            if (!state.getValue(POWERED)) {
                level.setBlock(pos, state.setValue(POWERED, true), 2);
                level.updateNeighborsAt(pos, this);
                level.scheduleTick(pos, this, 4);
            }
        }
        spawnRedstoneParticles(level, pos);
    }

    private void spawnRedstoneParticles(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 6; i++) {
                double x = pos.getX() + 0.25 + level.getRandom().nextDouble() * 0.5;
                double y = pos.getY() + 0.5 + level.getRandom().nextDouble() * 0.5;
                double z = pos.getZ() + 0.25 + level.getRandom().nextDouble() * 0.5;
                serverLevel.sendParticles(DustParticleOptions.REDSTONE, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PotBlockEntity pot) || pot.variant == null) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        if (pot.variant.traits().contains(PotTrait.TRAPPED)) {
            startSignal(level, pos);
        }

        if (pot.variant.traits().contains(PotTrait.INFESTED)) {
            int amount = level.getRandom().nextInt(1, 4);
            for (int i = 0; i < amount; i++) {
                Silverfish silverfish = EntityType.SILVERFISH.create(level);
                if (silverfish != null) {
                    silverfish.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                    level.addFreshEntity(silverfish);
                    silverfish.spawnAnim();
                }
            }

            level.destroyBlock(pos, true, player);

            return ItemInteractionResult.SUCCESS;
        }

        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean inserted = pot.insert(stack.copyWithCount(1));
        if (!inserted) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        pot.wobble(DecoratedPotBlockEntity.WobbleStyle.POSITIVE);

        player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        stack.shrink(1);

        level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1, 0.7F + 0.5F * pot.getFullness());

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.DUST_PLUME, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 7, 0, 0, 0, 0);
        }

        pot.setChanged();
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

        if (pot.isLiving()) {
            pot.wakeUp(player);
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PotBlockEntity pot)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        level.playSound(null, pos, SoundEvents.DECORATED_POT_INSERT_FAIL, SoundSource.BLOCKS, 1, 1);
        pot.wobble(DecoratedPotBlockEntity.WobbleStyle.NEGATIVE);

        if (pot.isLiving()) {
            pot.wakeUp(player);
        }

        return InteractionResult.SUCCESS;
    }


    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.variant != null) return pot.variant.shape();
        return Shapes.block();
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HORIZONTAL_FACING, BRITTLE, POWERED, WATERLOGGED);
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
            if (pot.variant == null) {
                List<Holder.Reference<PotVariant>> variants = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).holders().filter(variant -> variant.value().size() == size).toList();
                pot.variant = variants.get(level.getRandom().nextInt(variants.size())).value();
            } else if (state.getValue(BRITTLE) != pot.variant.traits().contains(PotTrait.BRITTLE)) state.setValue(BRITTLE, pot.variant.traits().contains(PotTrait.BRITTLE));
        }

        level.scheduleTick(pos, this, this.getDelayAfterPlace());
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.variant != null) {
                if (pot.getTheItem().getItem() instanceof LingeringPotionItem potion) {
                    level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.SPLASH_POTION_BREAK, SoundSource.NEUTRAL, 0.5F, 0.6F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
                    if (!level.isClientSide) {
                        Projectile projectile = potion.asProjectile(level, new Vec3(pos.getX(), pos.getY(), pos.getZ()), pot.getTheItem(), Direction.UP);
                        projectile.shoot(pos.getX(), pos.getY(), pos.getZ(), 0.5F, 1);
                        level.addFreshEntity(projectile);
                    }
                } else Containers.dropContents(level, pos, pot);

                if (pot.variant.traits().contains(PotTrait.INFESTED)) {
                    int amount = level.getRandom().nextInt(1, 4);
                    for (int i = 0; i < amount; i++) {
                        Silverfish silverfish = EntityType.SILVERFISH.create(level);
                        if (silverfish != null) {
                            silverfish.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
                            level.addFreshEntity(silverfish);
                            silverfish.spawnAnim();
                        }
                    }
                }

                if (pot.variant.traits().contains(PotTrait.TRAPPED)) {
                    spawnRedstoneParticles(level, pos);
                }

                if (level instanceof ServerLevel serverLevel && pot.variant.traits().contains(PotTrait.REGENERATES)) {
                    Registry<PotVariant> variants = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY);
                    RegeneratingPotsData.getOrDefault(serverLevel).addPot(pos, new PotData(state, variants.getKey(pot.variant)), level.getRandom().nextInt(20, 40)*20);
                }

                level.updateNeighbourForOutputSignal(pos, state.getBlock());
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    protected SoundType getSoundType(BlockState state) {
        return state.getValue(BRITTLE) ? SoundType.DECORATED_POT_CRACKED : SoundType.DECORATED_POT;
    }

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return (level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.variant != null && pot.variant.traits().contains(PotTrait.BRITTLE)) || player.getMainHandItem().canPerformAction(ItemAbilities.PICKAXE_DIG) ? 1 : super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.isLiving()) {
            pot.wakeUp(player);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && size == PotSize.SMALL
                && level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.isLiving()) {
            pot.wakeUp(null);
        }
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.isLiving()) {
            pot.wakeUp(entity instanceof LivingEntity le ? le : null);
        }
        super.stepOn(level, pos, state, entity);
    }

    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockpos = hit.getBlockPos();
        if (!level.isClientSide && projectile.mayInteract(level, blockpos) && projectile.mayBreak(level)) {
            if (level.getBlockEntity(blockpos) instanceof PotBlockEntity pot && pot.isLiving()) {
                pot.wakeUp(projectile.getOwner() instanceof LivingEntity le ? le : null);
            } else {
                level.destroyBlock(blockpos, true, projectile);
            }
        }
    }

    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof PotBlockEntity pot ? pot.getPotAsItem() : super.getCloneItemStack(level, pos, state);
    }

    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(HORIZONTAL_FACING, rotation.rotate(state.getValue(HORIZONTAL_FACING)));
    }

    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(HORIZONTAL_FACING)));
    }

    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock) {
        if (fallingBlock.getBlockState().getValue(BRITTLE) || fallingBlock.fallDistance > 4) {
            level.destroyBlock(pos, true);
        }
    }

    public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, BlockEntity blockEntity, Entity breaker, ItemStack tool) {
        if (blockEntity instanceof PotBlockEntity pot && pot.variant != null && pot.variant.traits().contains(PotTrait.DROPS_EXPERIENCE)) {
            return level.getRandom().nextInt(2, 6);
        }

        return 0;
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof PotBlockEntity pot && pot.variant != null && pot.variant.traits().contains(PotTrait.FLAMMABLE);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
            if (pot.getTheItem().getItem() instanceof LingeringPotionItem && level.getRandom().nextFloat() < 0.15) {
                PotionContents potionContents = pot.getTheItem().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
                int i = potionContents.equals(PotionContents.EMPTY) ? 0 : potionContents.getColor();
                ParticleOptions particle = ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, FastColor.ARGB32.color(80, i));

                double d0 = pos.getX() + 0.5 + random.nextInt(-40, 40) * 0.01;
                double d1 = pos.getY() + random.nextInt(-10, 40) * 0.001 + 0.8;
                double d2 = pos.getZ() + 0.5 + random.nextInt(-40, 40) * 0.01;

                level.addAlwaysVisibleParticle(particle, d0, d1, d2, 0, 0, 0);
            }
        }
    }

    protected int getDelayAfterPlace() {
        return 2;
    }

    public static boolean isFree(BlockState state) {
        return state.isAir() || state.is(BlockTags.FIRE) || state.liquid() || state.canBeReplaced();
    }
}
