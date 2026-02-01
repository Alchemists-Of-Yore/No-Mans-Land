package com.farcr.nomansland.common.blockentity;

import com.farcr.nomansland.common.dialogue.DialogueContainer;
import com.farcr.nomansland.common.dialogue.DialogueState;
import com.farcr.nomansland.common.dialogue.DialogueUtil;
import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.dialogue.DialogueRegistry.DialoguePool;
import com.farcr.nomansland.common.effect.FriendshipEffect;
import com.farcr.nomansland.common.networking.ClientboundDialoguePacket;
import com.farcr.nomansland.common.networking.ClientboundFriendMoonStatePacket;
import com.farcr.nomansland.common.networking.ClientboundMoonlightBasinTrackPacket;
import com.farcr.nomansland.common.registry.NMLBlockEntities;
import com.farcr.nomansland.common.registry.NMLCriteriaTriggers;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class MoonlightBasinBlockEntity extends BlockEntity {

    /*
    * A list of events the client can update the server with
    */
    public enum FriendMoonUpdatePacket {
        AWAKEN(0, (basin) -> {
            basin.moonAwake = true;
            basin.forFriendshipPlayers((player) -> {
                if (player.getEffect(NMLEffects.FRIENDSHIP) != null) {
                    NMLCriteriaTriggers.MEET_FRIEND_MOON.get().trigger(player);
                    PacketDistributor.sendToPlayer(player, new ClientboundFriendMoonStatePacket(FriendMoonRenderer.FriendMoonAnimation.TALKING));
                }
            }, false);
        });

        private int id;
        private Consumer<MoonlightBasinBlockEntity> consumer;
        FriendMoonUpdatePacket(int id, Consumer<MoonlightBasinBlockEntity> consumer) {
            this.id = id;
            this.consumer = consumer;
        }

        public int getId() { return id; }
        public Consumer<MoonlightBasinBlockEntity> getConsumer() { return consumer; }

        public static final IntFunction<FriendMoonUpdatePacket> BY_ID = ByIdMap.continuous(FriendMoonUpdatePacket::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, FriendMoonUpdatePacket> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FriendMoonUpdatePacket::getId);
    }

    public MoonlightBasinBlockEntity(BlockPos pos, BlockState blockState) {
        super(NMLBlockEntities.MOONLIGHT_BASIN.get(), pos, blockState);
    }

    private boolean hasResetValues = false;
    public boolean moonAwake = false;
    public boolean timeAllowed = false;

    public void packetUpdateEvent(FriendMoonUpdatePacket packetType) {
        packetType.getConsumer().accept(this);
        pulseUpdate();
    }

    private void pulseUpdate() {
        setChanged();
        assert level != null;
        level.sendBlockUpdated(
            worldPosition, getBlockState(),
            getBlockState(), 3
        );
    }

    /* Runs at nighttime */
    private void resetValues() {

        // Flag values as reset
        hasResetValues = true;
        moonAwake = false;
        timeAllowed = true;

        pulseUpdate();
    }

    private int dialogueTicks = 10;

    public static boolean isNightTime(Level level) {
        return (level.getSkyDarken() >= 10);
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.moonAwake = tag.getBoolean("MoonAwake");
        this.timeAllowed = tag.getBoolean("TimeAllowed");
    }

    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("MoonAwake", moonAwake);
        tag.putBoolean("TimeAllowed", timeAllowed);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    static void setPlayerFriendship(ServerPlayer serverPlayer, BlockPos pos) {
        // Apply the friendship status effect
        serverPlayer.addEffect(new MobEffectInstance(NMLEffects.FRIENDSHIP, 30, 0, true, false));

        // Update the player's tracked basin to this one
        PacketDistributor.sendToPlayer(serverPlayer, new ClientboundMoonlightBasinTrackPacket(pos));
    }

    public static boolean cannotObtainFriendship(ServerPlayer serverPlayer) {
        return (serverPlayer.getEffect(MobEffects.BAD_OMEN) != null);
    }

    private float friendshipMaxRange = 5;
    public void forFriendshipPlayers(@Nullable Consumer<ServerPlayer> consumer, boolean applyFriendship) {
        BlockPos blockPos = getBlockPos();
        AABB aabb = new AABB(blockPos).inflate(friendshipMaxRange);
        for (ServerPlayer serverPlayer : level.getEntitiesOfClass(ServerPlayer.class, aabb)) {
            if (applyFriendship && !cannotObtainFriendship(serverPlayer))
                setPlayerFriendship(serverPlayer, blockPos);

            if (consumer != null)
                consumer.accept(serverPlayer);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MoonlightBasinBlockEntity blockEntity) {
        if (!level.isClientSide()) {
            if (isNightTime(level)) {
                if (!blockEntity.hasResetValues)
                    blockEntity.resetValues();

                if (blockEntity.moonAwake)
                    blockEntity.moonTick();
            } else {
                if (blockEntity.timeAllowed) {
                    blockEntity.timeAllowed = false;
                    blockEntity.pulseUpdate();
                }
                blockEntity.hasResetValues = false;
            }
            // Give Players in vicinity Friendship effect if they don't have it
            blockEntity.forFriendshipPlayers(null, true);
        }
    }

    public void moonTick() {
        // Passive Interaction
        dialogueTicks = Math.max(dialogueTicks - 1, 0);
        if (dialogueTicks <= 0)
            sendRandomDialogue(NMLRegistries.PASSIVE_DIALOGUE_KEY);
    }

    public void sendRandomDialogue(ResourceKey<Registry<DialoguePool>> registryKey) {
        Registry<DialogueRegistry.DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(level, registryKey);
        Optional<Holder.Reference<DialoguePool>> optionalDialogue = dialogueRegistry.getRandom(level.getRandom());
        optionalDialogue.ifPresent(dialogueReference -> sendDialogue(dialogueRegistry.getKey(dialogueReference.value()), registryKey));
    }

    public void sendDialogue(ResourceLocation dialogueLocation, ResourceKey<Registry<DialoguePool>> registryKey) {
        // send packet to players
        forFriendshipPlayers((serverPlayer) -> {
            PacketDistributor.sendToPlayer(serverPlayer, new ClientboundDialoguePacket(dialogueLocation, registryKey.location()));
        }, false);

        // calculate dialogue length in ticks
        Registry<DialogueRegistry.DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(level, registryKey);
        DialogueContainer dialogueContainer = new DialogueContainer(dialogueRegistry.get(dialogueLocation).text());
        float deltaToTicks = ((60 / 20f) / 2f); // not sure why this works but it does
        dialogueTicks = (int) ((dialogueContainer.getTextLength() * (DialogueState.DIALOGUE_SPEED) * deltaToTicks));
        dialogueTicks += (20) * level.getRandom().nextIntBetweenInclusive(5, 8);
    }

    @Override
    public void onLoad() {

    }
}
