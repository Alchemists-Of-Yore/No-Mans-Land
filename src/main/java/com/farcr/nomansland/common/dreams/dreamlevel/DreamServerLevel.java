package com.farcr.nomansland.common.dreams.dreamlevel;

import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.networking.dream.ClientboundDreamPacket;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;

public class DreamServerLevel extends ServerLevel {
    private final DreamType.DreamTypeInstance dreamTypeInstance;
    public DreamType.DreamTypeInstance getDreamTypeInstance() {
        return dreamTypeInstance;
    }
    public DreamServerLevel(
        MinecraftServer server, Executor dispatcher,
        LevelStorageSource.LevelStorageAccess levelStorageAccess,
        ServerLevelData serverLevelData, ResourceKey<Level> dimension,
        LevelStem levelStem, ChunkProgressListener progressListener,
        boolean isDebug, long biomeZoomSeed, List<CustomSpawner> customSpawners,
        boolean tickTime, @Nullable RandomSequences randomSequences,
        DreamType.DreamTypeInstance dreamTypeInstance
    ) {
        super(
            server, dispatcher,
            levelStorageAccess,
            serverLevelData,
            dimension, levelStem,
            progressListener, isDebug,
            biomeZoomSeed, customSpawners,
            tickTime, randomSequences
        );
        this.dreamTypeInstance = dreamTypeInstance;
    }

    @Override
    public void tick(BooleanSupplier hasTimeLeft) {
        dreamTypeInstance.tick(this);
        super.tick(hasTimeLeft);
    }

    @Override public void tickPrecipitation(BlockPos blockPos) {}

    @Override public boolean mayInteract(Player player, BlockPos pos) { return false; }

    @Override public void save(@Nullable ProgressListener progress, boolean flush, boolean skipSave) {}

    public void endDream(boolean success) {
        DreamManager manager = DreamManager.getOrDefault(getServer());
        List<ServerPlayer> playerList = new ArrayList<>(players());
        for (ServerPlayer player : playerList) {
            DreamingPlayer dreamingPlayer = manager.getDreamingPlayer(player);
            if (dreamingPlayer != null) dreamingPlayer.discardTether();
            else DreamLevelHandler.playerTeleportFallback(player, false);

            dreamTypeInstance.getDreamType().onDreamEnd(getServer().getPlayerList().getPlayer(player.getUUID()), success);
            PacketDistributor.sendToPlayer(player,
                new ClientboundDreamPacket(Optional.empty())
            );

            if (success) manager.getPlayerStorage(player).setDreamExperienced();
        }
    }
}
