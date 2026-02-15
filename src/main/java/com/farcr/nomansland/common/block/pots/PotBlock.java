package com.farcr.nomansland.common.block.pots;

import com.farcr.nomansland.common.blockentity.PotBlockEntity;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.LingeringPotionItem;
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

    private final PotSize size;

    public PotBlock(PotSize size, Properties properties) {
        super(properties);
        this.size = size;
        registerDefaultState(stateDefinition.any().setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(BRITTLE, false));
    }

    public MapCodec<PotBlock> codec() {
        return CODEC;
    }

    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection()).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof PotBlockEntity pot)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }

        if (pot.variant.traits().contains(PotTrait.TRAPPED)) {
            // TODO: button/observer block
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

        if (level.isClientSide) {
            return ItemInteractionResult.CONSUME;
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

        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.DUST_PLUME, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 7, 0, 0, 0, 0);
        }

        pot.setChanged();
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);

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
        builder.add(HORIZONTAL_FACING, BRITTLE, WATERLOGGED);
    }

    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
            if (pot.variant == null) {
                List<Holder.Reference<PotVariant>> variants = level.registryAccess().registryOrThrow(NMLRegistries.POT_VARIANT_KEY).holders().filter(variant -> variant.value().size() == size).toList();
                pot.variant = variants.get(level.getRandom().nextInt(variants.size())).value();
            }

            if (state.getValue(BRITTLE) != pot.variant.traits().contains(PotTrait.BRITTLE)) state.setValue(BRITTLE, pot.variant.traits().contains(PotTrait.BRITTLE));
        }
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof PotBlockEntity pot) {
                if (pot.getTheItem().getItem() instanceof LingeringPotionItem lingeringPotionItem) {
                    level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.LINGERING_POTION_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
                    if (!level.isClientSide) {
                        Projectile projectile = lingeringPotionItem.asProjectile(level, new Vec3(pos.getX(), pos.getY(), pos.getZ()), pot.getTheItem(), Direction.UP);
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
                    // TODO: button/observer block somehow
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

    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        BlockPos blockpos = hit.getBlockPos();
        if (!level.isClientSide && projectile.mayInteract(level, blockpos) && projectile.mayBreak(level)) {
            level.destroyBlock(blockpos, true, projectile);
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
        if (blockEntity instanceof PotBlockEntity pot && pot.variant.traits().contains(PotTrait.DROPS_EXPERIENCE)) {
            return level.getRandom().nextInt(2, 6);
        }

        return 0;
    }
}
