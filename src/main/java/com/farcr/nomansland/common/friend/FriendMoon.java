package com.farcr.nomansland.common.friend;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.friend.dialogue.DialogueContainer;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.friend.dialogue.DialogueState;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import com.farcr.nomansland.common.networking.ClientboundDialoguePacket;
import com.farcr.nomansland.common.networking.ClientboundDialogueResetPacket;
import com.farcr.nomansland.common.networking.ClientboundMoonlightBasinTrackPacket;
import com.farcr.nomansland.common.registry.NMLCriteriaTriggers;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class FriendMoon extends SavedData {
    @Nullable private final ServerLevel level;
    public static final String NAME = "friend_moon";
    public FriendMoon(@Nullable ServerLevel level) {
        this.level = level;
        setDirty();
    }

    public static FriendMoon getOrDefault(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
            () -> new FriendMoon(level),
            (tag, provider) -> FriendMoon.create(tag, provider, level)
        ), FriendMoon.NAME);
    }

    public boolean awake = false;
    private int dialogueTicks = 10;
    public int getDialogueTicks() { return dialogueTicks; }
    public boolean isAwake() {
        return awake;
    }

    private FriendMoonState state = FriendMoonState.IDLE;
    public void setState(FriendMoonState newState) {
        this.state = newState;
    }

    public void resetValues() {
        awake = false;
        setDirty();
    }

    public FriendMoon load(CompoundTag tag, HolderLookup.Provider provider) {
        awake = tag.getBoolean("IsAwake");
        setState(FriendMoonState.CODEC.byName(tag.getString("State"), FriendMoonState.IDLE));
        return this;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("IsAwake", isAwake());
        tag.putString("State", state.getSerializedName());
        return tag;
    }

    public static FriendMoon create(CompoundTag tag, HolderLookup.Provider provider, ServerLevel serverLevel) {
        FriendMoon moon = new FriendMoon(serverLevel);
        return moon.load(tag, provider);
    }

    /* Friendship */
    public static boolean cannotObtainFriendship(ServerPlayer serverPlayer) {
        return (serverPlayer.getEffect(MobEffects.BAD_OMEN) != null);
    }

    public static final DeferredHolder<MobEffect, MobEffect> FRIENDSHIP = NMLEffects.FRIENDSHIP;
    public static void grantPlayerFriendship(ServerPlayer serverPlayer, BlockPos pos) {
        if (!cannotObtainFriendship(serverPlayer)) {
            serverPlayer.addEffect(new MobEffectInstance(FRIENDSHIP, 30, 0, true, false));
            PacketDistributor.sendToPlayer(serverPlayer, new ClientboundMoonlightBasinTrackPacket(pos));
        }
    }

    public void forFriendshipPlayers(Consumer<ServerPlayer> consumer) {
        assert level != null;
        for (ServerPlayer serverPlayer : level.getPlayers((player) -> {return player.hasEffect(FRIENDSHIP);}))
            consumer.accept(serverPlayer);
    }

    public static boolean isNightTime(Level level) {
        return (level.getSkyDarken() >= 10);
    }

    /* Behavior */
    public void tick() {
        if (isAwake()) {
            if (!isNightTime(level)) {
                resetValues();
                return;
            }

            // Grant players advancement if they do not have it
            AtomicInteger playerTracker = new AtomicInteger();
            forFriendshipPlayers((player) -> {
                NMLCriteriaTriggers.MEET_FRIEND_MOON.get().trigger(player);
                playerTracker.getAndIncrement();
            });
            // Ensure players are listening to the Moon
            int totalPlayers = playerTracker.get();
            if (totalPlayers > 0) {
                // Passive Dialogue
                if (dialogueTicks >= 0) {
                    dialogueTicks = Math.max(dialogueTicks - 1, 0);
                    if (dialogueTicks == 0)
                        sendRandomDialogue(NMLRegistries.PASSIVE_DIALOGUE_KEY);
                }
            }
        }
    }

    public void packetUpdateEvent(FriendMoonUpdate packetType) {
        packetType.getConsumer().accept(this);
        this.setDirty();
    }

    public void sendRandomDialogue(ResourceKey<Registry<DialogueRegistry.DialoguePool>> registryKey) {
        Registry<DialogueRegistry.DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(level, registryKey);
        WeightedRandomList<DialogueRegistry.DialoguePool> weightedList = WeightedRandomList.create(dialogueRegistry.stream().toList());
        sendDialogue(dialogueRegistry.getKey(DialogueUtil.getWeightedEntry(weightedList, level.getRandom())), registryKey);
    }

    public void resetDialogue() {
        forFriendshipPlayers((serverPlayer) -> {PacketDistributor.sendToPlayer(serverPlayer, new ClientboundDialogueResetPacket());});
        dialogueTicks = -1;
    }

    public void sendDialogue(ResourceLocation dialogueLocation, ResourceKey<Registry<DialogueRegistry.DialoguePool>> registryKey) {
        forFriendshipPlayers((serverPlayer) -> {
            PacketDistributor.sendToPlayer(serverPlayer, new ClientboundDialoguePacket(dialogueLocation, registryKey.location()));
        });

        // calculate dialogue length in ticks
        Registry<DialogueRegistry.DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(level, registryKey);
        DialogueContainer dialogueContainer = new DialogueContainer(dialogueRegistry.get(dialogueLocation).text());

        // not sure why this works but it does
        float deltaToTicks = ((60 / 20f) / 2f);
        dialogueTicks = (int) ((dialogueContainer.getTextLength() * (DialogueState.DIALOGUE_SPEED) * deltaToTicks));
        dialogueTicks += (20) * level.getRandom().nextIntBetweenInclusive(5, 8);
    }
}
