package com.farcr.nomansland.common.friend;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.blockentity.MoonlightBasinBlockEntity;
import com.farcr.nomansland.common.entity.buddy.Buddy;
import com.farcr.nomansland.common.friend.condition.MoonlightContextualConditions;
import com.farcr.nomansland.common.friend.condition.MoonlightGreetingConditions;
import com.farcr.nomansland.common.friend.condition.MoonlightLeavingConditions;
import com.farcr.nomansland.common.friend.dialogue.*;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialoguePacket;
import com.farcr.nomansland.common.networking.dialogue.ClientboundDialogueResetPacket;
import com.farcr.nomansland.common.networking.friend.ClientboundMeetingPointPacket;
import com.farcr.nomansland.common.networking.friend.ClientboundMoonlightBasinTrackPacket;
import com.farcr.nomansland.common.networking.friend.FriendMoonUpdatePacket;
import com.farcr.nomansland.common.registry.NMLCriteriaTriggers;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;
import com.farcr.nomansland.common.registry.entities.NMLEffects;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

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

    public List<UUID> upsetWith = new ArrayList<>();
    public void setUpsetWith(UUID playerUUID) {
        if (!upsetWith.contains(playerUUID)) {
            upsetWith.add(playerUUID);
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
        candleTimer = tag.getInt("CandleTimer");
        setState(FriendMoonState.CODEC.byName(tag.getString("State"), FriendMoonState.PASSIVE));
        updatedShadow = tag.getBoolean("UpdatedShadow");

        upsetWith.clear();
        for (Tag uuidEntry : tag.getList("UpsetWith", 10))
            upsetWith.add(NbtUtils.loadUUID(uuidEntry));

        // read player map
        playerPositionMap.clear();
        for (Tag storedTag : tag.getList("StoredPlayerPositions", 10)) {
            if (storedTag instanceof CompoundTag dataTag) {
                playerPositionMap.put(
                    dataTag.getUUID("UUID"),
                    NbtUtils.readBlockPos(dataTag, "pos").orElseThrow()
                );
            }
        }
        return this;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putBoolean("IsAwake", isAwake());
        tag.putInt("CandleTimer", getCandleTime());
        tag.putString("State", state.getSerializedName());
        tag.putBoolean("UpdatedShadow", updatedShadow);

        ListTag listTag = new ListTag();
        upsetWith.forEach((playerUUID) -> listTag.add(NbtUtils.createUUID(playerUUID)));
        tag.put("UpsetWith", listTag);

        // store player map
        ListTag positionTag = new ListTag();
        playerPositionMap.forEach((uuid, blockPos) -> {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("UUID", uuid);
            playerTag.put("pos", NbtUtils.writeBlockPos(blockPos));
            positionTag.add(playerTag);
        });
        tag.put("StoredPlayerPositions", positionTag);

        return tag;
    }

    public static FriendMoon create(CompoundTag tag, HolderLookup.Provider provider, ServerLevel serverLevel) {
        FriendMoon moon = new FriendMoon(serverLevel);
        return moon.load(tag, provider);
    }

    /* Friendship */
    public boolean cannotObtainFriendship(Player serverPlayer) {
        return (serverPlayer.getEffect(MobEffects.BAD_OMEN) != null)
            || upsetWith.contains(serverPlayer.getUUID());
    }

    public static final DeferredHolder<MobEffect, MobEffect> FRIENDSHIP = NMLEffects.FRIENDSHIP;
    public static void grantPlayerFriendship(FriendMoon friendMoon, ServerPlayer serverPlayer, BlockPos pos) {
        if (friendMoon.getState() != FriendMoonState.UPSET && !friendMoon.cannotObtainFriendship(serverPlayer))
            serverPlayer.addEffect(new MobEffectInstance(FRIENDSHIP, 30, 0, true, false));
        else friendMoon.setUpsetWith(serverPlayer.getUUID());
        PacketDistributor.sendToPlayer(serverPlayer, new ClientboundMoonlightBasinTrackPacket(pos));
    }

    public List<ServerPlayer> getFriendshipPlayers() {
        return level.getPlayers((player) -> player.hasEffect(FRIENDSHIP));
    }

    private final HashMap<ServerPlayer, Integer> lastFriendshipPlayers = new HashMap<>();
    public void forFriendshipPlayers(Consumer<ServerPlayer> consumer) {
        assert level != null;
        for (ServerPlayer serverPlayer : getFriendshipPlayers())
            consumer.accept(serverPlayer);
    }

    public static boolean isSpecialInteraction(MoonlightBasinBlockEntity.OfferingContext offeringContext) {
        return (offeringContext.isValid() && offeringContext.entity().getType().equals(NMLEntities.BUDDY.get()));
    }
    public boolean specialInteraction(Level level, Entity entity) {
        if (entity instanceof Buddy buddy) {
            level.addParticle(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MYCELIUM.defaultBlockState()),
                buddy.getX(), buddy.getY() + 1, buddy.getZ(), 0, 0, 0
            );
            return true;
        }
        return false;
    }

    public static boolean isNightTime(Level level) {
        return (level.getSkyDarken() >= 10);
    }

    public static boolean displayFriendShadow(Player player) {
        return true; // TEMPORARY, eventually only display under the condition the player has seen the dream
    }

    private boolean updatedShadow = false;
    private final HashMap<UUID, BlockPos> playerPositionMap = new HashMap<>();
    public void updateMeetingPointInformation(Level level) {
        if (isNightTime(level)) {
            if (!updatedShadow) {
                level.players().forEach((player) -> {
                    if (player instanceof ServerPlayer serverPlayer) {
                        if (displayFriendShadow(serverPlayer))
                            playerPositionMap.put(serverPlayer.getUUID(), serverPlayer.blockPosition());
                        setDirty();
                        updatePlayerFriendShadow(serverPlayer);
                    }
                });
                updatedShadow = true;
            }
        } else updatedShadow = false;
    }

    public static BlockPos getMeetingPointPosition(ServerLevel level) {
        ChunkPos meetingPointChunk = level.getChunkSource().getGeneratorState().meetingPointPosition();
        if (meetingPointChunk == null)
            return null;
        return meetingPointChunk.getMiddleBlockPosition(0);
    }

    public void updatePlayerFriendShadow(ServerPlayer player) {
        assert level != null;
        UUID playerUUID = player.getUUID();
        if (playerPositionMap.containsKey(playerUUID)) {
            Optional<BlockPos> lastPosition = Optional.ofNullable(playerPositionMap.get(playerUUID));
            Optional<BlockPos> meetingPointPosition = Optional.ofNullable(getMeetingPointPosition(level));
            PacketDistributor.sendToPlayer(player, new ClientboundMeetingPointPacket(lastPosition, meetingPointPosition));
        }
    }

    /* Behavior */
    public void tick() {
        assert level != null;
        updateMeetingPointInformation(level);

        if (!isNightTime(level) && !upsetWith.isEmpty()) {
            upsetWith.clear();
            setDirty();
        }

        if (isAwake()) {
            if (!isNightTime(level)) {
                resetValues();
                return;
            }

            // query players that had friendship
            for (ServerPlayer player : lastFriendshipPlayers.keySet()) {
                if (player != null && (!player.hasEffect(FRIENDSHIP) || player.isDeadOrDying())) {
                    lastFriendshipPlayers.put(player, lastFriendshipPlayers.get(player) + 1);
                    if (lastFriendshipPlayers.get(player) >= 5 || player.isDeadOrDying()) {
                        if (!cannotObtainFriendship(player)) {
                            getDialogueFromStream(NMLRegistries.LEAVING_DIALOGUE_KEY,
                                (registry) -> leavingFilter(registry, player))
                                    .dispatch(level, player);
                        }
                        lastFriendshipPlayers.remove(player);
                    }
                }
            }
            // Grant players advancement if they do not have it
            AtomicInteger playerTracker = new AtomicInteger();
            forFriendshipPlayers((player) -> {
                playerTracker.getAndIncrement();
                // and store the player for later
                lastFriendshipPlayers.put(player, 0);
                if (getState() != FriendMoonState.GREETING)
                    NMLCriteriaTriggers.MEET_FRIEND_MOON.get().trigger(player);
            });

            // Ensure players are listening to the Moon
            int totalPlayers = playerTracker.get();
            if (totalPlayers > 0) {
                if (getState() == FriendMoonState.GREETING) {
                    queryUniqueDialogue(NMLRegistries.GREETING_DIALOGUE_KEY, this::greetingFilter);
                    return;
                }

                // Passive Dialogue
                if (dialogueTicks >= 0) {
                    dialogueTicks = Math.max(dialogueTicks - 1, 0);
                    if (dialogueTicks == 0) {
                        getState().getMoonConsumer().accept(this);
                        randomDialogue(getState().getDialoguePoolType());
                    }
                }
            } else if (getState() == FriendMoonState.OFFERING)
                setState(FriendMoonState.PASSIVE);
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

    public void packetUpdateEvent(FriendMoonUpdate.ToServer packetType) {
        packetType.getConsumer().accept(this);
        this.setDirty();
    }

    private static final int DIALOGUE_PADDING = 100;
    public void applyDialogueLength(int dialogueLength) {
        if (dialogueLength > 0) {
            dialogueTicks = (dialogueLength + DIALOGUE_PADDING);
            setDirty();
        }
    }

    private DialogueLocation getDialogueFromStream(
        ResourceKey<Registry<DialoguePool>> registryKey,
        Function<Registry<DialoguePool>, DialoguePool> consumer
    ) {
        Registry<DialoguePool> registry = DialogueUtil.getDialogueRegistry(level, registryKey);
        DialoguePool resultingPool = consumer.apply(registry);
        return new DialogueLocation(
            (resultingPool != null ? registry.getKey(resultingPool) : null),
            registryKey.location(),
            level.getRandom()
        );
    }

    public DialogueLocation getDialogueFromLocation(
        ResourceKey<Registry<DialoguePool>> registryKey,
        ResourceLocation dialogueLocation
    ) {
        return new DialogueLocation(
            dialogueLocation,
            registryKey.location(),
            level.getRandom()
        );
    }

    /*
    * Queries possible unique dialogue per players
    * and applies the highest delay possible
    *
    * technically this is wrong as it dispatches wholly unique dialogue to each player,
    * but for now that's fine until I want to start messing with the passive dialogue
    */
    private void queryUniqueDialogue(
        ResourceKey<Registry<DialoguePool>> registryKey,
        BiFunction<Registry<DialoguePool>, ServerPlayer, DialoguePool> function
    ) {
        assert level != null;
        AtomicInteger highestTicks = new AtomicInteger();
        forFriendshipPlayers((serverPlayer) -> {
            int localDialogueTicks = getDialogueFromStream(registryKey,
                (registry) -> function.apply(registry, serverPlayer))
                .dispatch(level, serverPlayer);
            if (highestTicks.get() < localDialogueTicks)
                highestTicks.set(localDialogueTicks);

        });
        applyDialogueLength(highestTicks.get());
        getState().getMoonConsumer().accept(this);
    }

    /*
    * Dialogue Queries: Dialogue Queries provide different conditions for sending dialogue.
    * The way I've set the system up is done in a way where you provide the query and it should
    * allow you to sort through any kind of condition you wish to for dialogues and dispatch them automatically
    * without having to write the same redundant code that gets the registry and resourcelocation
    */
    private final ResourceLocation MEET_MOON_ADVANCEMENT = NoMansLand.location("main/meet_friend_moon");
    private DialoguePool greetingFilter(Registry<DialoguePool> registry, ServerPlayer serverPlayer) {
        List<DialoguePool> filteredDialogue = registry.stream().filter(
            (dialoguePool) -> (dialoguePool.condition().isEmpty())).toList();

        // Grant Advancement
        AdvancementHolder meetAdvancement = level.getServer().getAdvancements().get(MEET_MOON_ADVANCEMENT);
        NMLCriteriaTriggers.MEET_FRIEND_MOON.get().trigger(serverPlayer);

        if (meetAdvancement != null && !serverPlayer.getAdvancements().getOrStartProgress(meetAdvancement).isDone())
            filteredDialogue = MoonlightGreetingConditions.FirstTimeGreetingConditional.FIRST_TIME_ARRAY;

        return DialogueUtil.getWeightedEntry(filteredDialogue, level.getRandom());
    }


    private DialoguePool leavingFilter(Registry<DialoguePool> registry, ServerPlayer serverPlayer) {
        List<DialoguePool> filteredDialogue = registry.stream().filter(
            (dialoguePool) -> (dialoguePool.condition().isEmpty())).toList();

        if (serverPlayer.isDeadOrDying())
            filteredDialogue = MoonlightLeavingConditions.OnDeathConditional.ON_DEATH_ARRAY;

        return DialogueUtil.getWeightedEntry(filteredDialogue, level.getRandom());
    }

    // Simple dialogue filter used in most cases, just selects a random dialogue based on weight
    private DialoguePool weightedFilter(Registry<DialoguePool> registry) {
        return DialogueUtil.getWeightedEntry(registry.stream().toList(), level.getRandom());
    }

    public ServerPlayer getContextualPlayer() {
        for (ServerPlayer serverPlayer : level.getPlayers((player) -> {return player.hasEffect(FRIENDSHIP);})) {
            if (level.getBlockState(serverPlayer.blockPosition()).is(NMLBlocks.MOONLIGHT_BASIN))
                return serverPlayer;
        }
        return null;
    }

    public boolean contextualDialogue(ServerPlayer serverPlayer) {
        DialogueLocation dialogueLocation = getDialogueFromStream(NMLRegistries.CONTEXTUAL_DIALOGUE_KEY, (registry) -> {
            List<DialoguePool> filteredDialogue = registry.stream().filter(
                (dialoguePool) -> (dialoguePool.condition().isEmpty())).toList();

            // Talk about more interesting things if theyre available
            ArrayList<DialoguePool> conditionalDialogue = new ArrayList<>();

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

            return DialogueUtil.getWeightedEntry(filteredDialogue, level.getRandom());
        }).setTargetPlayer(serverPlayer);
        int dialogueLength = dialogueLocation.dispatch(level, getFriendshipPlayers());
        applyDialogueLength(dialogueLength);
        // if the dialogue length is greater than 0 it succeeded
        return (dialogueLength > 0);
    }

    public void randomDialogue(ResourceKey<Registry<DialoguePool>> registryKey) {
        if (registryKey == null)
            return;
        // Contextual Dialogue
        if (registryKey == NMLRegistries.PASSIVE_DIALOGUE_KEY) {
            ServerPlayer contextualPlayer = getContextualPlayer();
            if (contextualPlayer != null && contextualDialogue(contextualPlayer))
                return;
        }
        // Passive / Otherwise
        applyDialogueLength(
            getDialogueFromStream(registryKey, this::weightedFilter)
                .dispatch(level, getFriendshipPlayers())
        );
    }

    public void resetDialogue(boolean clientSide) {
        if (!clientSide)
           forFriendshipPlayers((serverPlayer) -> {PacketDistributor.sendToPlayer(serverPlayer, new ClientboundDialogueResetPacket());});
        dialogueTicks = -1;
    }
}
