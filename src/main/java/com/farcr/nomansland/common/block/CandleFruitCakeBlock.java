package com.farcr.nomansland.common.block;

import com.farcr.nomansland.common.integration.FDIntegration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CandleFruitCakeBlock extends AbstractCandleBlock {

    public static final MapCodec<CandleFruitCakeBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("candle").forGetter(block -> block.candleBlock),
                    propertiesCodec()
            ).apply(instance, CandleFruitCakeBlock::new));
    public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
    protected static final VoxelShape CAKE_SHAPE = Block.box(1.0, 0.0, 1.0, 15.0, 8.0, 15.0);
    protected static final VoxelShape CANDLE_SHAPE = Block.box(7.0, 8.0, 7.0, 9.0, 14.0, 9.0);
    protected static final VoxelShape SHAPE = Shapes.or(CAKE_SHAPE, CANDLE_SHAPE);
    private static final Map<CandleBlock, CandleFruitCakeBlock> BY_CANDLE = Maps.newHashMap();
    private static final Iterable<Vec3> PARTICLE_OFFSETS = ImmutableList.of(new Vec3(0.5, 1.0, 0.5));
    private final CandleBlock candleBlock;

    public MapCodec<CandleFruitCakeBlock> codec() {
        return CODEC;
    }

    public CandleFruitCakeBlock(Block candleBlock, BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
        if (candleBlock instanceof CandleBlock candleblock) {
            BY_CANDLE.put(candleblock, this);
            this.candleBlock = candleblock;
        } else {
            String var10002 = String.valueOf(CandleBlock.class);
            throw new IllegalArgumentException("Expected block to be of " + var10002 + " was " + String.valueOf(candleBlock.getClass()));
        }
    }

    protected Iterable<Vec3> getParticleOffsets(BlockState state) {
        return PARTICLE_OFFSETS;
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE)) {
            if (candleHit(hitResult) && stack.isEmpty() && state.getValue(LIT)) {
                extinguish(player, state, level, pos);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
            }
        } else {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        InteractionResult interactionresult = CakeBlock.eat(level, pos, FDIntegration.FRUIT_CAKE.block().defaultBlockState(), player);
        if (interactionresult.consumesAction()) {
            dropResources(state, level, pos);
        }

        return interactionresult;
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        if (itemAbility.equals(ItemAbilities.FIRESTARTER_LIGHT) && canLight(state)) {
            return state.setValue(LIT, true);
        }

        return null;
    }

    private static boolean candleHit(BlockHitResult hit) {
        return hit.getLocation().y - (double)hit.getBlockPos().getY() > 0.5;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return FDIntegration.FRUIT_CAKE.stack();
    }

    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return CakeBlock.FULL_CAKE_SIGNAL;
    }

    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    public static BlockState byCandle(CandleBlock candle) {
        return BY_CANDLE.get(candle) == null ? candle.defaultBlockState() : BY_CANDLE.get(candle).defaultBlockState();
    }

    public static boolean canLight(BlockState state) {
        return state.is(BlockTags.CANDLE_CAKES, base -> base.hasProperty(LIT) && !base.getValue(LIT));
    }
}
