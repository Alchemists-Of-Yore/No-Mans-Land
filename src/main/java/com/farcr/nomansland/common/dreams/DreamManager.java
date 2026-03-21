package com.farcr.nomansland.common.dreams;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamingPlayer;
import com.farcr.nomansland.common.networking.dream.ClientboundDreamStartPacket;
import com.farcr.nomansland.common.registry.NMLDreamTypes;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

/*
* Wanted to write more generalized code for this, maybe some of the specifics can be adapted later
* but for now this only needs to be used for one thing, the Friend Moon Dream
 */
public class DreamManager extends SavedData {
    public final ServerLevel level;
    public static final String NAME = "dream_manager";

    public static List<DreamType.DreamTypeInstance> instanceList = new ArrayList<>();
    public static void buildDreamTypeContext() {
        instanceList.clear();
        NMLRegistries.DREAM_TYPE.holders().forEach((reference)
            -> instanceList.add(new DreamType.DreamTypeInstance(reference.value())));
    }

    public DreamManager(ServerLevel level) {
        this.level = level;
    }
    public static DreamManager getOrDefault(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(() -> new DreamManager(level),
            (tag, provider) -> DreamManager.create(tag, provider, level)
        ), DreamManager.NAME);
    }

    Map<UUID, DreamStorage> storageMap = new HashMap<>();
    public DreamStorage getPlayer(ServerPlayer player) {
        return storageMap.getOrDefault(player.getUUID(), new DreamStorage());
    }

    // eventually this will be changed to (player, dream)
    public boolean playerHasExperiencedDream(ServerPlayer player, DreamType dreamType) {
        return getPlayer(player).getHasExperiencedDream();
    }

    // should not be serialized or stored as when the server starts unloading all players should return to their dreaming players
    public final Map<ServerPlayer, DreamingPlayer> dreamerMap = new HashMap<>();
    public DreamingPlayer getDreamingPlayer(ServerPlayer serverPlayer, ServerLevel level, DreamType dreamType) {
        if (!dreamerMap.containsKey(serverPlayer)) {
            DreamingPlayer dreamPlayer = new DreamingPlayer(level, serverPlayer);

            level.addNewPlayer(dreamPlayer);

            dreamerMap.put(serverPlayer, dreamPlayer);
        }
        return dreamerMap.get(serverPlayer);
    }

    public void notifyClient(ServerPlayer player) {
        DreamType dreamType = playerGetDream(player);
        if (dreamType != null && player.isSleeping()) {
            ServerLevel previousLevel = player.serverLevel();
            ServerLevel level = DreamLevelHandler.getDreamLevel(player.server, dreamType, player);

            // needs to be sync list instead of add to list, its own packet as well
            PacketDistributor.sendToPlayer(player, new ClientboundDreamStartPacket(
                NMLRegistries.DREAM_TYPE.getKey(dreamType)
            ));

            DreamingPlayer dreamPlayer = getDreamingPlayer(player, level, dreamType);
        }
    }

    public DreamType playerGetDream(ServerPlayer player) {
        for (DreamType.DreamTypeInstance dreamTypeInstance : instanceList) {
            BiFunction<ServerPlayer, ServerLevel, Boolean> function = dreamTypeInstance.dreamType.biconsumer;
            if (function != null && !playerHasExperiencedDream(player, dreamTypeInstance.dreamType) && function.apply(player, level))
                return dreamTypeInstance.dreamType;
        }
        return null;
    }

    public boolean playerShouldDream(ServerPlayer player) {
        return (playerGetDream(player) != null);
    }

    public static DreamManager create(CompoundTag tag, HolderLookup.Provider provider, ServerLevel serverLevel) {
        DreamManager manager = new DreamManager(serverLevel);
        return manager.load(tag, provider);
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

    public static boolean isDreamingPlayer(LivingEntity entity, boolean canDreamClientside) {
        Level level = entity.level();
        if (!entity.isAlive())
            return false;
        if (entity instanceof ServerPlayer player && level instanceof ServerLevel serverLevel)
            return DreamManager.getOrDefault(serverLevel).playerShouldDream(player);
        if (canDreamClientside && entity instanceof LocalPlayer localPlayer) {
            return level.isClientSide && localPlayer.equals(Minecraft.getInstance().player)
                && Client.getInstance().clientIsDreaming();
        }
        return false;
    }

    public static class Client implements AutoCloseable {
        public static DreamManager.Client INSTANCE;
        public static DreamManager.Client getInstance() {
            if (INSTANCE == null)
                INSTANCE = new DreamManager.Client();
            return INSTANCE;
        }

        @Override
        public void close() {}

        public static void destroy() {
            if (INSTANCE == null)
                return;
            INSTANCE.close();
            INSTANCE = null;
        }

        private DreamType dream;
        public void clientEndDream() {
            dream = null;
        }

        public void clientSetDream(DreamType dream) {
            clientEndDream();
            this.dream = dream;
        }

        public void tick() {
            if (!clientIsDreaming())
                return;

            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                // Clear Dreams
                if (!player.isAlive())
                    clientEndDream();

                if (dreamShouldRender()) {}
            }

        }

        public DreamType getDream() {
            return dream;
        }

        public boolean clientIsDreaming() {
            return (dream != null);
        }

        public int getRawTicks() {
            return Minecraft.getInstance()
                .player.getSleepTimer();
        }

        private static final int MAX_SLEEP_TICKS = 100;
        public boolean dreamShouldRender() {
            return (clientIsDreaming() && (getRawTicks() >= MAX_SLEEP_TICKS));
        }

        private float storedTicks = 0f;
        public float getSleepTicks(DeltaTracker deltaTracker) {
            if (dreamShouldRender()) {
                storedTicks += (deltaTracker.getGameTimeDeltaTicks() / 2.5f);
                return Math.max(0f, MAX_SLEEP_TICKS - storedTicks);
            }
            storedTicks = 0f;
            return getRawTicks();
        }

        public void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
            float alpha = (getSleepTicks(deltaTracker) / MAX_SLEEP_TICKS);
            int i = FastColor.ARGB32.colorFromFloat(alpha, 0f, 0f, 0f);
            guiGraphics.fill(RenderType.guiOverlay(), 0, 0,
                guiGraphics.guiWidth(), guiGraphics.guiHeight(), i);
        }
    }
}
