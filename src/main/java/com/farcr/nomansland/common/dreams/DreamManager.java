package com.farcr.nomansland.common.dreams;

import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamingPlayer;
import com.farcr.nomansland.common.networking.dream.ClientboundDreamPacket;
import com.farcr.nomansland.common.registry.NMLDreamTypes;
import com.farcr.nomansland.common.registry.NMLRegistries;
import com.farcr.nomansland.common.registry.entities.NMLEntities;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

/*
* Wanted to write more generalized code for this, maybe some of the specifics can be adapted later
* but for now this only needs to be used for one thing, the Friend Moon Dream
 */
public class DreamManager extends SavedData {
    public static final String NAME = "dream_manager";
    private final MinecraftServer server;
    public DreamManager(MinecraftServer server) {
        this.server = server;
    }

    public static DreamManager getOrDefault(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(() -> new DreamManager(server),
            (tag, provider) -> DreamManager.create(tag, provider, server)
        ), DreamManager.NAME);
    }

    Map<UUID, DreamStorage> storageMap = new HashMap<>();
    public DreamStorage getPlayerStorage(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!storageMap.containsKey(uuid)) storageMap.put(uuid, new DreamStorage());
        return storageMap.get(uuid);
    }

    public DreamStorage clearPlayerStorage(ServerPlayer player) {
        DreamStorage dreamStorage = storageMap.remove(player.getUUID());
        setDirty();
        return dreamStorage;
    }

    // eventually this will be changed to (player, dream)
    public boolean playerHasExperiencedDream(ServerPlayer player, DreamType dreamType) {
        if (!storageMap.containsKey(player.getUUID())) return false;
        return getPlayerStorage(player).getHasExperiencedDream();
    }

    // should not be serialized or stored as when the server starts unloading all players should return to their dreaming players
    private final Map<UUID, DreamingPlayer> dreamerMap = new HashMap<>();
    public int getDreamingPlayerCount() {
        int i = 0;
        for (UUID uuid : dreamerMap.keySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null)
                continue;
            if (getDreamingPlayer(player) == null)
                continue;
            i++;
        }
        return i;
    }

    public static boolean IGNORE_UPDATE_CONTEXT = false;
    public void createDreamingPlayer(ServerPlayer serverPlayer) {
        UUID playerUUID = serverPlayer.getUUID();
        if (!dreamerMap.containsKey(playerUUID)
        || (dreamerMap.get(playerUUID) == null)
        || (!dreamerMap.get(playerUUID).isAlive())) {
            ServerLevel level = serverPlayer.serverLevel();
            DreamingPlayer dreamPlayer = NMLEntities.DREAMING_PLAYER.get().create(level);
            dreamPlayer.setTetheredPlayer(serverPlayer);
            level.addFreshEntity(dreamPlayer);
            dreamerMap.put(playerUUID, dreamPlayer);
        }
    }

    public DreamingPlayer getDreamingPlayer(ServerPlayer player) {
        DreamingPlayer dreamingPlayer = dreamerMap.get(player.getUUID());
        if (dreamingPlayer != null && dreamingPlayer.isAlive()) return dreamingPlayer;
        return null;
    }

    public void transferSleep(ServerPlayer player, DreamType dreamType) {
        ServerLevel dreamLevel = DreamLevelHandler.getDreamLevel(player.server, dreamType, player);
        // summon fake player
        createDreamingPlayer(player);
        // suppress updating the player sleep counter
        IGNORE_UPDATE_CONTEXT = true;

        // move player to other dimension
        player.stopSleeping();
        player.changeDimension(
            new DimensionTransition(
                dreamLevel, dreamType.spawnPoint,
                dreamType.spawnPoint, 0, 0,
                DimensionTransition.DO_NOTHING
            )
        );
    }

    public void notifyClient(ServerPlayer player) {
        DreamType dreamType = playerGetDream(player);
        if (dreamType != null && player.isSleeping())
            forceNotify(dreamType, player);
    }

    public void forceNotify(DreamType dreamType, ServerPlayer player) {
        DreamLevelHandler.getDreamLevel(player.server, dreamType, player);
        PacketDistributor.sendToPlayer(player, new ClientboundDreamPacket(
            Optional.ofNullable(NMLRegistries.DREAM_TYPE.getKey(dreamType))
        ));
    }

    List<DreamType> instanceList = NMLDreamTypes.DREAM_TYPES_REGISTRY.getRegistry().get().stream().toList();
    public DreamType playerGetDream(ServerPlayer player) {
        for (DreamType dreamType : instanceList) {
            BiFunction<ServerPlayer, ServerLevel, Boolean> function = dreamType.biconsumer;
            if (function != null && !playerHasExperiencedDream(player, dreamType) && function.apply(player, player.serverLevel()))
                return dreamType;
        }
        return null;
    }

    public static boolean innerDreaming(DreamType dreamType, Player player) {
        return player.level().dimension().equals(
            DreamLevelHandler.resourceKey(
                Registries.DIMENSION, NMLDreamTypes.DREAM_TYPES_REGISTRY
                    .getRegistry().get().getKey(dreamType),
                player
            )
        );
    }

    public static DreamType.DreamTypeInstance getAmbiguousDreamTypeInstance(Player player) {
        if (player instanceof ServerPlayer serverPlayer && DreamManager.getOrDefault(serverPlayer.getServer()).playerIsDreaming(serverPlayer))
            return DreamLevelHandler.getDreamLevel(player.getServer(), DreamManager.getOrDefault(player.getServer()).playerGetDream(serverPlayer), serverPlayer)
                .getDreamTypeInstance();
        if (player.isLocalPlayer() && ClientDreamRenderer.getInstance().dreamShouldRender())
            return ClientDreamRenderer.getInstance().getDreamClientInstance();
        return null;
    }

    public static DreamType getAmbiguousDreamType(Player player) {
        if (player instanceof ServerPlayer serverPlayer && DreamManager.getOrDefault(serverPlayer.getServer()).playerIsDreaming(serverPlayer))
            return DreamManager.getOrDefault(serverPlayer.getServer()).playerGetDream(serverPlayer);
        if (player.isLocalPlayer() && ClientDreamRenderer.getInstance().dreamShouldRender())
            return ClientDreamRenderer.getInstance().getDream();
        return null;
    }

    public boolean playerIsDreaming(ServerPlayer player) {
        DreamType dreamType = playerGetDream(player);
        if (dreamType == null)
            return false;
        return innerDreaming(dreamType, player);
    }

    public boolean playerShouldDream(ServerPlayer player) {
        return (playerGetDream(player) != null);
    }

    public static DreamManager create(CompoundTag tag, HolderLookup.Provider provider, MinecraftServer server) {
        return new DreamManager(server).load(tag, provider);
    }

    public DreamManager load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        storageMap.clear();
        for (Tag tag : compoundTag.getList("Players", 10)) {
            if (tag instanceof CompoundTag playerDataTag) {
                storageMap.put(
                    playerDataTag.getUUID("UUID"),
                    DreamStorage.CODEC.parse(NbtOps.INSTANCE,
                        playerDataTag.get("DreamStorage")).getOrThrow()
                );
            }
        }
        return this;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        storageMap.forEach((playerUUID, dreamInfo) -> {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("UUID", playerUUID);
            tag.put("DreamStorage", DreamStorage.CODEC.encodeStart(NbtOps.INSTANCE, dreamInfo).getOrThrow());
            listTag.add(tag);
        });
        compoundTag.put("Players", listTag);
        return compoundTag;
    }

    public void setDreamExperienced(DreamType dreamType, ServerPlayer player) {
        getPlayerStorage(player).setDreamExperienced();
        setDirty();
    }
}
