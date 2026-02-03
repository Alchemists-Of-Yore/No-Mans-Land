package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class MoonlightBasinBlockEntity extends BlockEntity {

    public MoonlightBasinBlockEntity(BlockPos pos, BlockState blockState) {
        super(NMLBlockEntities.MOONLIGHT_BASIN.get(), pos, blockState);
        pulseUpdate();
    }

    private void pulseUpdate() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(
                getBlockPos(), getBlockState(),
                getBlockState(), 3
            );
        }
    }

    private static final AABB BASIN_BOUNDING_BOX =
        Block.box(-8d, 11d, -8d, 24d, 24d, 24d)
            .toAabbs().getFirst();

    public static ItemEntity getItemAbove(BlockPos pos, Level level) {
        AABB aabb = BASIN_BOUNDING_BOX.move(pos);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, aabb, EntitySelector.ENTITY_STILL_ALIVE))
            return item;
        return null;
    }

    private final float friendshipMaxRange = 5;
    public static void tick(Level level, BlockPos pos, BlockState state, MoonlightBasinBlockEntity blockEntity) {
        if (!level.isClientSide() && FriendMoon.isNightTime(level)) {
            AABB aabb = new AABB(pos).inflate(blockEntity.friendshipMaxRange);
            for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, aabb))
                FriendMoon.grantPlayerFriendship(serverPlayer, pos);

            FriendMoon friendMoon = FriendMoon.getOrDefault(level.getServer().overworld());
            if (friendMoon.isDirty())
                blockEntity.pulseUpdate();
        }

        // Query items above
        ItemEntity itemEntity = getItemAbove(pos, level);
        if (itemEntity != null && itemEntity.onGround()) {
            Vec3 newPosition = new Vec3(pos.getCenter().x, itemEntity.position().y, pos.getCenter().z);
            itemEntity.setDeltaMovement(new Vec3(0, 0, 0));
            itemEntity.addDeltaMovement(
                newPosition.subtract(itemEntity.position()).multiply(
                    new Vec3(new Vector3f(1 / 5f))
                )
            );
        }
    }

    public FriendMoon clientMoon;
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (getLevel().isClientSide()) {
            if (clientMoon == null)
                clientMoon = new FriendMoon(null);
            clientMoon.load(tag, registries);
        }
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        // Store Friend Moon information in BlockEntity
        assert level != null;
        if (!level.isClientSide()) {
            FriendMoon friendMoon = FriendMoon.getOrDefault(level.getServer().overworld());
            friendMoon.save(tag, registries);
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }
}
