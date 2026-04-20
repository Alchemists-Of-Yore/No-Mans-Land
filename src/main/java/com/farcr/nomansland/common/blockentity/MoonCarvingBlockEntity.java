package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.block.AncestralCarvingBlock;
import com.farcr.nomansland.common.block.MoonCarvingBlock;
import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.DreamStorage;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.networking.ClientboundZoomEffectPacket;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLDreamTypes;
import com.farcr.nomansland.common.registry.NMLSounds;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class MoonCarvingBlockEntity extends BlockEntity {
    public MoonCarvingBlockEntity(BlockPos pos, BlockState blockState) {
        super(NMLBlockEntities.MOON_CARVING.get(), pos, blockState);
    }

    private static final int VISION_RANGE = 24;
    private static final int STARE_AT_TICKS = 40;
    private static final int DREAM_TIME = 24000 * 3;

    private static final DreamType MOONLIGHT_DREAM_TYPE
        = NMLDreamTypes.FRIEND_MOON_DREAM.get();

    private final Map<Player, Integer> playerStareMap = new HashMap<>();
    private boolean playerMeetsCondition(ServerPlayer player) {
        DreamStorage storage = DreamManager.getOrDefault(player.getServer()).getPlayerStorage(player);
        return (storage.getTimeRemainingForDream(MOONLIGHT_DREAM_TYPE) <= 0)
            && (!storage.getHasExperiencedDream(MOONLIGHT_DREAM_TYPE));
    }

    private boolean blockStateMeetsConditions(Player player, BlockPos pos, BlockState blockState) {
        Direction blockDirection = blockState.getValue(BlockStateProperties.FACING);
        boolean playerIsFacingBlock = blockDirection.equals(player.getDirection().getOpposite());
        if (!playerIsFacingBlock) {
            if (blockDirection == Direction.DOWN && player.getXRot() < -20f)
                playerIsFacingBlock = true;
            if (blockDirection == Direction.UP && player.getXRot() > 20f)
                playerIsFacingBlock = true;
        }
        return playerIsFacingBlock && !((MoonCarvingBlock) blockState.getBlock()).queryPositions(
            level, pos, blockState.getValue(AncestralCarvingBlock.FACING),
            blockState.getValue(AncestralCarvingBlock.ROTATION),
            (blockState1) -> !blockState1.is(NMLBlocks.MOON_CARVING)
        );
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MoonCarvingBlockEntity blockEntity) {
        if (!level.isClientSide) {
            AABB boundingBox = new AABB(pos).inflate(VISION_RANGE);
            for (Player player : level.players()) {
                Map<Player, Integer> map = blockEntity.playerStareMap;
                if (boundingBox.contains(player.getX(), player.getY(), player.getZ())
                && blockEntity.playerMeetsCondition((ServerPlayer) player)) {
                    BlockHitResult cast = level.clip(
                        new ClipContext(
                            player.getEyePosition(),
                            player.getEyePosition().add(player.getViewVector(1.0f)
                                .multiply(new Vec3(VISION_RANGE, VISION_RANGE, VISION_RANGE))),
                            ClipContext.Block.VISUAL,
                            ClipContext.Fluid.NONE,
                            CollisionContext.empty()
                        )
                    );
                    if (cast.getType() == HitResult.Type.BLOCK && cast.getBlockPos().equals(pos)
                    && blockEntity.blockStateMeetsConditions(player, pos, state)) {
                        map.put(player, map.getOrDefault(player, 0) + 1);
                        if (map.get(player) > STARE_AT_TICKS) {
                            DreamManager.getOrDefault(player.getServer())
                                .getPlayerStorage((ServerPlayer) player)
                                .setTimeRemainingForDream(
                                    MOONLIGHT_DREAM_TYPE,
                                    DREAM_TIME
                                );
                            level.playSound(
                                null, pos,
                                NMLSounds.MOON_CARVING_ACTIVATE.get(),
                                SoundSource.AMBIENT
                            );
                            PacketDistributor.sendToPlayer((ServerPlayer) player,
                                new ClientboundZoomEffectPacket(70));
                        }
                    } else
                        map.remove(player);
                } else
                    map.remove(player);
            }
        }
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
