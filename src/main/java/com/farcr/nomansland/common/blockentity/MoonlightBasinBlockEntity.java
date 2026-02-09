package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.moonlight.MoonlightCandleBlock;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.farcr.nomansland.common.friend.FriendMoonState;
import com.farcr.nomansland.common.friend.condition.MoonlightOfferingConditions;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Optional;

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

    public record OfferingContext(Entity entity, ResourceLocation dialogueLocation){
        public boolean isValid() {
            if (entity instanceof ItemEntity itemEntity) {
                if (itemEntity.getItem().getItem() == Items.AIR)
                    return false;
            }
            return entity != null && entity.isAlive();
        }
    };

    public static OfferingContext getOfferingAbove(BlockPos pos, Level level) {
        AABB aabb = BASIN_BOUNDING_BOX.move(pos);
        for (Entity entity : level.getEntitiesOfClass(Entity.class, aabb, EntitySelector.ENTITY_STILL_ALIVE)) {
            if (entity.NML$wasPreviouslyInspected())
                continue;

            ArrayList<DialogueRegistry.DialoguePool> list = new ArrayList<>();
            // Item Offering List
            if (entity instanceof ItemEntity itemEntity) {
                Item itemType = itemEntity.getItem().getItem().asItem();
                list = DialogueUtil.iterateTags(
                    itemType, level.registryAccess(), Registries.ITEM,
                    MoonlightOfferingConditions.ItemOfferingConditional.COMPILED_MAP,
                    MoonlightOfferingConditions.ItemOfferingConditional.KEY_MAP,
                    list
                );
            }
            // Entity Offering List
            list = DialogueUtil.iterateTags(
                entity.getType(), level.registryAccess(), Registries.ENTITY_TYPE,
                MoonlightOfferingConditions.EntityOfferingConditional.COMPILED_MAP,
                MoonlightOfferingConditions.EntityOfferingConditional.KEY_MAP,
                list
            );

            if (!list.isEmpty()) {
                DialogueRegistry.DialoguePool pool = DialogueUtil.getWeightedEntry(WeightedRandomList.create(list), level.getRandom());
                Optional<Registry<DialogueRegistry.DialoguePool>> optionalRegistry = level.registryAccess().registry(NMLRegistries.OFFERING_DIALOGUE_KEY);
                if (optionalRegistry.isPresent())
                    return new OfferingContext(entity, optionalRegistry.get().getKey(pool));
            }
        }
        return null;
    }

    private final float friendshipMaxRange = 9;

    private OfferingContext inspectionContext;
    private void setInspectionContext(OfferingContext newInspectionContext, FriendMoon friendMoon) {
        // clear previous inspection context
        if (inspectionContext != null)
            inspectionContext.entity().NML$setInspectionState(false);

        // assign new inspection context
        inspectionContext = newInspectionContext;
        if (newInspectionContext != null)
            newInspectionContext.entity().NML$setInspectionState(true);

        if (friendMoon != null && newInspectionContext != null)
            friendMoon.setState(FriendMoonState.OFFERING);
    }

    public static final int BLOCK_Y_REACH = 3;
    public static final int BLOCK_EXTEND_REACH = 10;

    public static ArrayList<BlockPos> getCandles(Level level, BlockPos basinPosition) {
        ArrayList<BlockPos> candles = new ArrayList<>();

        int rotation = 270;
        int travelAlong = 0;
        int segmentLength = 1;
        int segmentProgress = 0;
        Vec3i vectorOffset = new Vec3i(0, 0, 0);
        for (int i = 1; i <= (BLOCK_EXTEND_REACH * BLOCK_EXTEND_REACH); i++) {
            for (int j = -BLOCK_Y_REACH; j < BLOCK_Y_REACH; j++) {
                BlockPos elevatedPosition = basinPosition.offset(vectorOffset).offset(0, j, 0);
                BlockState potentialCandle = level.getBlockState(elevatedPosition);
                if (potentialCandle.is(NMLBlocks.MOONLIGHT_CANDLE))
                    candles.add(new BlockPos(elevatedPosition));
            }
            double radians = rotation * (Math.PI / 180.0);
            vectorOffset = vectorOffset.offset((int) Math.round(Math.sin(radians)), 0, (int) Math.round(Math.cos(radians)));

            travelAlong++;
            if (travelAlong >= segmentLength) {
                travelAlong = 0;
                rotation += 90;
                if (rotation >= 360)
                    rotation -= 360;

                segmentProgress++;
                if (segmentProgress % 2 == 0)
                    segmentLength++;
            }
        }
        return candles;
    }

    private int trackedCandles = -1;
    private boolean queryNegativeInteraction(ArrayList<BlockPos> candleList) {
        int litCandles = queryLitCandles(candleList);
        boolean returnValue = (litCandles < trackedCandles);
        trackedCandles = litCandles;
        return returnValue;
    }

    private int queryLitCandles(ArrayList<BlockPos> candleList) {
        int litCandles = 0;
        for (BlockPos candlePos : candleList) {
            assert level != null;
            if (level.getBlockState(candlePos).getValue(MoonlightCandleBlock.CANDLE_LIT))
                litCandles++;
        }
        return litCandles;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MoonlightBasinBlockEntity blockEntity) {
        @Nullable FriendMoon friendMoon = (!level.isClientSide() ? FriendMoon.getOrDefault(level.getServer().overworld()) : blockEntity.clientMoon);
        if (friendMoon == null)
            return;

        if (!level.isClientSide()) {
            if (FriendMoon.isNightTime(level)) {
                AABB aabb = new AABB(pos).inflate(blockEntity.friendshipMaxRange);
                for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, aabb))
                    FriendMoon.grantPlayerFriendship(friendMoon, serverPlayer, pos);
                if (friendMoon.shouldPulseUpdate())
                    blockEntity.pulseUpdate();
            } else
                blockEntity.trackedCandles = 0;
        }

        if (!friendMoon.isActive())
            return;

        OfferingContext inspectionContext = blockEntity.inspectionContext;
        if (inspectionContext == null || !inspectionContext.isValid()) {
            // Query items above
            OfferingContext context = getOfferingAbove(pos, level);
            if (context != null && context.entity().onGround()) {
                Entity entity = context.entity();
                Vec3 newPosition = new Vec3(pos.getCenter().x, entity.position().y, pos.getCenter().z);
                entity.setDeltaMovement(new Vec3(0, 0, 0));

                if (!entity.NML$isBeingInspected()) {
                    Vec3 approachSpeed = newPosition.subtract(entity.position()).multiply(
                        new Vec3(new Vector3f(1 / 15f))
                    );
                    entity.addDeltaMovement(approachSpeed);
                    if (approachSpeed.lengthSqr() <= 0.001f) {
                        entity.setDeltaMovement(new Vec3(0d, 0d, 0d));
                        blockEntity.setInspectionContext(context, friendMoon);
                    }
                }
                friendMoon.resetDialogue(level.isClientSide());
            }
        } else {
            if (friendMoon.getState() == FriendMoonState.OFFERING) {
                // Inspecting Entity behavior
                Entity inspect = inspectionContext.entity();

                Vec3 raisedPosition = pos.above(2).getCenter();
                Vec3 dist = raisedPosition.subtract(inspect.position());
                float speed = 1 / 20f;
                inspect.setDeltaMovement(
                    dist.multiply(new Vec3(new Vector3f(speed)))
                );

                if (!level.isClientSide() && friendMoon.getDialogueTicks() < 0)
                    friendMoon.sendDialogue(inspectionContext.dialogueLocation(), NMLRegistries.OFFERING_DIALOGUE_KEY);
            } else
                blockEntity.setInspectionContext(null, friendMoon);
        }

        if (!level.isClientSide()) {
            // Check Candles at the end
            ArrayList<BlockPos> candleList = getCandles(level, pos);
            if (blockEntity.queryNegativeInteraction(candleList)) {
                friendMoon.negative();
                if (blockEntity.trackedCandles <= 0)
                    friendMoon.setState(FriendMoonState.UPSET);
            } else if (friendMoon.getCandleTime() <= 0) {
                candleList.forEach((blockPos) -> {
                    BlockState blockState = level.getBlockState(blockPos);
                    if (!blockState.getValue(MoonlightCandleBlock.CANDLE_LIT))
                        level.setBlock(blockPos, blockState.setValue(MoonlightCandleBlock.CANDLE_LIT, true), 3);
                });
            }
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
