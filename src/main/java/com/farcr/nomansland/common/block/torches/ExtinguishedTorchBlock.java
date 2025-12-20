package com.farcr.nomansland.common.block.torches;

import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.NMLTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ExtinguishedTorchBlock extends TorchBlock {

    public final Block litBlock;

    public ExtinguishedTorchBlock(SimpleParticleType flameParticle, Properties properties) {
        super(flameParticle, properties);

        Block litBlock = null;
        for (ExtinguishableBlock block : NMLRegistries.EXTINGUISHABLE_BLOCKS) {
            if (this == block.extinguishedBlock()) {
                litBlock = block.litBlock();
                break;
            }
        }

        this.litBlock = litBlock;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.getItemInHand(hand).is(NMLTags.FIRESTARTERS)) {
            level.playSound(player,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    stack.is(Items.FLINT_AND_STEEL) ? NMLSounds.TORCH_LIGHT_BY_FLINT_AND_STEEL : NMLSounds.TORCH_LIGHT,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F);
            level.setBlock(pos, litBlock.defaultBlockState(), 3);
            if (!level.isClientSide) {
                ServerLevel serverLevel = (ServerLevel) level;
                serverLevel.sendParticles(flameParticle, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 5, 0, 0, 0, 0);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack stack, boolean dropExperience) {
        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, level.random.nextInt(2, 7), 0, 0, 0, 0.05);
    }

    @Override
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult hit, Projectile projectile) {
        if (!level.isClientSide && projectile.isOnFire()) {
            level.setBlock(hit.getBlockPos(), this.litBlock.withPropertiesOf(state), 11);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return litBlock.getCloneItemStack(state, target, level, pos, player);
    }

    @Override
    public String getDescriptionId() {
        return litBlock.getDescriptionId();
    }
}
