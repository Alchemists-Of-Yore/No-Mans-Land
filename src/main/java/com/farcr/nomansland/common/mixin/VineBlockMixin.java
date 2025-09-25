package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static net.minecraft.world.level.block.VineBlock.getPropertyForFace;

@Mixin(VineBlock.class)
public abstract class VineBlockMixin implements BonemealableBlock {

    @Shadow protected abstract boolean canSupportAtFace(BlockGetter level, BlockPos pos, Direction direction);

    @Redirect(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Direction;getRandom(Lnet/minecraft/util/RandomSource;)Lnet/minecraft/core/Direction;"))
    private Direction nml$getRandom(RandomSource random) {
        Direction direction = Util.getRandom(Direction.values(), random);
        while (direction != Direction.DOWN && direction != Direction.UP) {
            direction = Util.getRandom(Direction.values(), random);
        }
        return direction;
    }

    @WrapOperation(method = "canSupportAtFace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean nml$isCutVine(BlockState instance, Block block, Operation<Boolean> original) {
        if (block == NMLBlocks.CUT_VINE.get() && instance.is(Blocks.VINE)) return true;
        if (instance.is(NMLBlocks.CUT_VINE.block())) return false;
        return original.call(instance, block);
    }

    @WrapOperation(method = "getUpdatedState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean nml$isCutVine1(BlockState instance, Block block, Operation<Boolean> original) {
        if (block == NMLBlocks.CUT_VINE.get() && instance.is(Blocks.VINE)) return true;
        if (instance.is(NMLBlocks.CUT_VINE.block())) return false;
        return original.call(instance, block);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        int x = blockPos.getX();
        int y = blockPos.getY();
        int z = blockPos.getZ();

        for (BlockPos bp : BlockPos.betweenClosed(x - 3, y - 3, z - 3, x + 3, y + 3, z + 3)) {
            BlockState vineState = Blocks.VINE.defaultBlockState();
            if (serverLevel.getBlockState(bp).isEmpty() && serverLevel.random.nextBoolean()) {
                for (Direction d : Direction.values()) {
                    if (d == Direction.DOWN) continue;
                    BooleanProperty booleanproperty = getPropertyForFace(d);
                    vineState = vineState.setValue(booleanproperty, canSupportAtFace(serverLevel, bp, d));
                }
                if (vineState != Blocks.VINE.defaultBlockState()) {
                    serverLevel.setBlockAndUpdate(bp, vineState);
                }
            }
        }
    }
}
