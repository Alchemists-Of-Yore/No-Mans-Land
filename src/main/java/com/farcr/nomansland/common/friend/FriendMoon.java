package com.farcr.nomansland.common.friend;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.friend.condition.MoonlightContextualConditions;
import com.farcr.nomansland.common.friend.condition.MoonlightGreetingConditions;
import com.farcr.nomansland.common.friend.dialogue.DialogueContainer;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.friend.dialogue.DialogueState;
import com.farcr.nomansland.common.friend.dialogue.DialogueUtil;
import com.farcr.nomansland.common.networking.ClientboundDialoguePacket;
import com.farcr.nomansland.common.networking.ClientboundDialogueResetPacket;
import com.farcr.nomansland.common.networking.ClientboundMoonlightBasinTrackPacket;
import com.farcr.nomansland.common.registry.NMLCriteriaTriggers;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class FriendMoon extends SavedData {
    @Nullable private final ServerLevel level;
    public static final String NAME = "friend_moon";
    public FriendMoon(@Nullable ServerLevel level) {
        this.level = level;
    }

    public static FriendMoon getOrDefault(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
            () -> new FriendMoon(level),
            (tag, provider) -> FriendMoon.create(tag, provider, level)
        ), FriendMoon.NAME);
    }

    public boolean awake = false;
    public boolean isAwake() {
        return awake;
    }
    public boolean isActive() {
        return isAwake() && (getState() != FriendMoonState.UPSET);
    }

    private int dialogueTicks = 10;
    public int getDialogueTicks() { return dialogueTicks; }

    private int candleTimer = 0;
    public int getCandleTime() { return candleTimer; }
    public void setCandleTime(int candleTime) {
        this.candleTimer = candleTime;
    }

    private FriendMoonState state = FriendMoonState.GREETING;
    public FriendMoonState getState() { return this.state; }
    public void setState(FriendMoonState newState) {
        if (newState != state) {
            this.state = newState;
            setDirty();
        }
    }

    public void resetValues() {
        awake = false;
        setState(FriendMoonState.GREETING);
        setCandleTime(-1);

        setDirty();
    }

    boolean pulseUpdate = false;
    @Override public void setDirty() {
        pulseUpdate = true;
        super.setDirty();
    }
    public boolean shouldPulseUpdate() {
        if (pulseUpdate) {
            pulseUpdate = false;
            return true;
        }
        return false;
    }

    public FriendMoon load(CompoundTag tag, HolderLookup.Provider provider) {
        awake = tag.getBoolean("IsAwake");
        setState(FriendMoonState.CODEC.byName(tag.getString("State"), FriendMoonState.PASSIVE));
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
    public static void grantPlayerFriendship(FriendMoon friendMoon, ServerPlayer serverPlayer, BlockPos pos) {
        if (!cannotObtainFriendship(serverPlayer) && (friendMoon.getState() != FriendMoonState.UPSET)) {
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
        if (isAwake() && level != null) {
            if (!isNightTime(level)) {
                resetValues();
                return;
            }

            // Grant players advancement if they do not have it
            AtomicInteger playerTracker = new AtomicInteger();
            forFriendshipPlayers((player) -> {
                playerTracker.getAndIncrement();
                if (getState() != FriendMoonState.GREETING)
                    NMLCriteriaTriggers.MEET_FRIEND_MOON.get().trigger(player);
            });

            // Ensure players are listening to the Moon
            int totalPlayers = playerTracker.get();
            if (totalPlayers > 0) {
                if (getState() == FriendMoonState.GREETING) {
                    sendGreetingDialogue();
                    return;
                }

                // Passive Dialogue
                if (dialogueTicks >= 0) {
                    dialogueTicks = Math.max(dialogueTicks - 1, 0);
                    if (dialogueTicks == 0) {
                        getState().getMoonConsumer().accept(this);
                        sendRandomDialogue(getState().getDialoguePoolType());
                    }
                }
            }
        }
    }

    int NEGATIVE_TIME = 40;
    public void negative() {
        setCandleTime(3);
        setState(FriendMoonState.NEGATIVE);

        // Dialogue Reset
        resetDialogue(false);
        dialogueTicks = NEGATIVE_TIME;
    }

    public void packetUpdateEvent(FriendMoonUpdate packetType) {
        packetType.getConsumer().accept(this);
        this.setDirty();
    }

    private final ResourceLocation MEET_MOON_ADVANCEMENT = NoMansLand.location("main/meet_friend_moon");
    public void sendGreetingDialogue() {
        assert level != null;
        AtomicInteger highestTicks = new AtomicInteger();
        forFriendshipPlayers((serverPlayer) -> {
            Registry<DialogueRegistry.DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(level, NMLRegistries.GREETING_DIALOGUE_KEY);
            List<DialogueRegistry.DialoguePool> filteredDialogue = dialogueRegistry.stream().filter(
                (dialoguePool) -> (dialoguePool.condition().isEmpty())).toList();

            // Query individual dialogue based on if the player has met the moon before or not
            AdvancementHolder meetAdvancement = level.getServer().getAdvancements().get(MEET_MOON_ADVANCEMENT);
            if (meetAdvancement != null && !serverPlayer.getAdvancements().getOrStartProgress(meetAdvancement).isDone())
                filteredDialogue = MoonlightGreetingConditions.FirstTimeGreetingConditional.FIRST_TIME_ARRAY;

            DialogueRegistry.DialoguePool poolSelection = DialogueUtil.getWeightedEntry(WeightedRandomList.create(filteredDialogue), level.getRandom());
            ResourceLocation dialogueLocation = dialogueRegistry.getKey(poolSelection);

            PacketDistributor.sendToPlayer(serverPlayer, new ClientboundDialoguePacket(
                    dialogueLocation, NMLRegistries.GREETING_DIALOGUE_KEY.location(), Optional.empty()));
            DialogueContainer dialogueContainer = new DialogueContainer(dialogueRegistry.get(dialogueLocation).text());

            // Because we are querying individual dialogues, the moon will wait for the longest one to run its course before sending another
            int localDialogueTicks = getDialogueTicks(dialogueContainer.getTextLength());
            if (highestTicks.get() < localDialogueTicks)
                highestTicks.set(localDialogueTicks);

            // Grant Advancement
            NMLCriteriaTriggers.MEET_FRIEND_MOON.get().trigger(serverPlayer);
        });
        dialogueTicks = highestTicks.get();
        getState().getMoonConsumer().accept(this);
    }

    public ServerPlayer getContextualPlayer() {
        for (ServerPlayer serverPlayer : level.getPlayers((player) -> {return player.hasEffect(FRIENDSHIP);})) {
            NoMansLand.LOGGER.info(level.getBlockState(serverPlayer.blockPosition()).getBlock());
            if (level.getBlockState(serverPlayer.blockPosition()).is(NMLBlocks.MOONLIGHT_BASIN))
                return serverPlayer;
        }
        return null;
    }

    public boolean sendContextualDialogue(ServerPlayer serverPlayer) {
        Registry<DialogueRegistry.DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(level, NMLRegistries.CONTEXTUAL_DIALOGUE_KEY);
        List<DialogueRegistry.DialoguePool> filteredDialogue = dialogueRegistry.stream().filter(
                (dialoguePool) -> (dialoguePool.condition().isEmpty())).toList();

        // Talk about more interesting things if theyre available
        ArrayList<DialogueRegistry.DialoguePool> conditionalDialogue = new ArrayList<>();

        // Check Equipment
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = serverPlayer.getItemBySlot(slot);
            if (!item.isEmpty()) {
                DialogueUtil.appendTags(
                    item.getItem(), level.registryAccess(), Registries.ITEM,
                    MoonlightContextualConditions.EquipmentContextualConditional.COMPILED_MAP,
                    MoonlightContextualConditions.EquipmentContextualConditional.KEY_MAP,
                    conditionalDialogue
                );
            }
        }

        // Check Effects
        serverPlayer.getActiveEffects().forEach((effect) -> {
            DialogueUtil.appendTags(
                effect.getEffect().value(), level.registryAccess(), Registries.MOB_EFFECT,
                MoonlightContextualConditions.EffectContextualCondition.COMPILED_MAP,
                MoonlightContextualConditions.EffectContextualCondition.KEY_MAP,
                conditionalDialogue
            );
        });

        if (!conditionalDialogue.isEmpty())
            filteredDialogue = conditionalDialogue;

        // Don't present contextual dialogue when there is none
        if (filteredDialogue.isEmpty())
            return false;

        DialogueRegistry.DialoguePool poolSelection = DialogueUtil.getWeightedEntry(WeightedRandomList.create(filteredDialogue), level.getRandom());
        ResourceLocation dialogueLocation = dialogueRegistry.getKey(poolSelection);

        sendDialogue(dialogueLocation, NMLRegistries.CONTEXTUAL_DIALOGUE_KEY, Optional.of(serverPlayer.getUUID()));
        return true;
    }

    public void sendRandomDialogue(ResourceKey<Registry<DialogueRegistry.DialoguePool>> registryKey) {
        if (registryKey == null)
            return;
        else if (registryKey == NMLRegistries.PASSIVE_DIALOGUE_KEY) {
            ServerPlayer contextualPlayer = getContextualPlayer();
            if (contextualPlayer != null && sendContextualDialogue(contextualPlayer))
                return;
        }
        Registry<DialogueRegistry.DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(level, registryKey);
        WeightedRandomList<DialogueRegistry.DialoguePool> weightedList = WeightedRandomList.create(dialogueRegistry.stream().toList());
        sendDialogue(dialogueRegistry.getKey(DialogueUtil.getWeightedEntry(weightedList, level.getRandom())), registryKey);
    }

    public void resetDialogue(boolean clientSide) {
        if (!clientSide)
           forFriendshipPlayers((serverPlayer) -> {PacketDistributor.sendToPlayer(serverPlayer, new ClientboundDialogueResetPacket());});
        dialogueTicks = -1;
    }

    // not sure why this works but it does
    float DELTA_TO_TICKS = ((60 / 20f) / 2f);

    public int getDialogueTicks(int textLength) {
        return (int) ((textLength * (DialogueState.DIALOGUE_SPEED) * DELTA_TO_TICKS))
            + ((20) * level.getRandom().nextIntBetweenInclusive(5, 8));
    }

    public void sendDialogue(ResourceLocation dialogueLocation, ResourceKey<Registry<DialogueRegistry.DialoguePool>> registryKey) {
        this.sendDialogue(dialogueLocation, registryKey, Optional.empty());
    }

    public void sendDialogue(ResourceLocation dialogueLocation, ResourceKey<Registry<DialogueRegistry.DialoguePool>> registryKey, Optional<UUID> playerUUID) {
        forFriendshipPlayers((serverPlayer) -> {
            PacketDistributor.sendToPlayer(serverPlayer, new ClientboundDialoguePacket(dialogueLocation, registryKey.location(), playerUUID));
        });

        // calculate dialogue length in ticks
        Registry<DialogueRegistry.DialoguePool> dialogueRegistry = DialogueUtil.getDialogueRegistry(level, registryKey);
        DialogueContainer dialogueContainer = new DialogueContainer(dialogueRegistry.get(dialogueLocation).text());

        dialogueTicks += getDialogueTicks(dialogueContainer.getTextLength());
    }
}
