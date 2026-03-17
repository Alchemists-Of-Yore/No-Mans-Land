package com.farcr.nomansland.client.dreams;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.mixin.client.ClientPacketListenerMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.Services;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.DimensionTransitionScreenManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.connection.ConnectionType;

import java.io.Closeable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DreamLevelHandler implements AutoCloseable {

    private final ClientLevel fakeLevel;
    public ClientLevel getFakeLevel() {
        return fakeLevel;
    }

    private final ClientLevel originalLevel;
    public ClientLevel getOriginalLevel() {
        return originalLevel;
    }

    private final LevelRenderer renderer;
    private final ClientPacketListener packetListener;

    private final Minecraft mc = Minecraft.getInstance();
    public DreamLevelHandler(LevelRenderer levelRenderer) {
        this.renderer = levelRenderer;
        ClientLevel level = mc.level;

        // what the fuck were they thinking
        CommonListenerCookie bullshit = new CommonListenerCookie(
            mc.getGameProfile(),
            mc.getTelemetryManager()
                .createWorldSessionManager(false, null, null),
            mc.player.registryAccess().freeze(),
            FeatureFlagSet.of(),
            null,
            mc.getCurrentServer(),
            null,
            Collections.emptyMap(),
            mc.gui.getChat().storeState(),
            false,
            Collections.emptyMap(),
            ServerLinks.EMPTY,
            ConnectionType.NEOFORGE
        );

        packetListener = new ClientPacketListener(
            mc, mc.getConnection().getConnection(), bullshit);

        fakeLevel = new ClientLevel(
            packetListener, level.getLevelData(),
            level.dimension(), level.dimensionTypeRegistration(),
            8, 4,
            mc::getProfiler, levelRenderer,
            level.isDebug(), level.getBiomeManager().biomeZoomSeed
        );
        packetListener.level = fakeLevel;
        originalLevel = level;

        fakeLevel.getWorldBorder().setSize(16d);

        LocalPlayer localPlayer = mc.player;
        localPlayer.setPos(new Vec3(0, 2, 0));
        fakeLevel.players().add(localPlayer);

        setFakeLevel();
    }

    private final Map<ChunkPos, LevelChunk> chunkMap = new HashMap<>();

    private LevelChunk getChunk(ChunkPos chunkPos) {
        LevelChunk chunk = new LevelChunk(fakeLevel, chunkPos);
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                BlockPos blockPos = new BlockPos(i, 0, j);
                chunk.setBlockState(
                    blockPos,
                    Blocks.DIRT.defaultBlockState(),
                    false
                );
                fakeLevel.setBlocksDirty(blockPos, Blocks.AIR.defaultBlockState(),
                    Blocks.DIRT.defaultBlockState());
            }
        }
        return chunk;
    }

    private void populateChunkPos(ChunkPos chunkPos) {
        if (!chunkMap.containsKey(chunkPos)) {

            // Create Chunk
            LevelChunk serverChunk = getChunk(chunkPos);
            serverChunk.setLoaded(true);
            chunkMap.put(chunkPos, serverChunk);

            ClientboundChunksBiomesPacket data = new ClientboundChunksBiomesPacket(
                List.of(new ClientboundChunksBiomesPacket.ChunkBiomeData(serverChunk)));
            packetListener.handleChunksBiomes(data);

            ClientboundLevelChunkWithLightPacket packet =
                new ClientboundLevelChunkWithLightPacket(
                    serverChunk,
                    fakeLevel.getLightEngine(),
                    null,
                    null
                );

            packetListener.handleLevelChunkWithLight(packet);
        }
    }

    public void tick() {
        Player player = mc.player;
        assert player != null;

        ChunkPos chunkPos = player.chunkPosition();
        for (int i = -4; i < 4; i++) {
            for (int j = -4; j < 4; j++) {
                populateChunkPos(new ChunkPos(chunkPos.x + i, chunkPos.z + j));
            }
        }

        fakeLevel.tick(() -> true);
    }

    public void setFakeLevel() {
        mc.player.setLevel(fakeLevel);
        mc.cameraEntity.setLevel(fakeLevel);
        mc.updateLevelInEngines(fakeLevel);
    }


    @Override
    public void close() {
        ClientLevel originalLevel = getOriginalLevel();
        mc.player.setLevel(originalLevel);
        mc.cameraEntity.setLevel(originalLevel);
        mc.updateLevelInEngines(originalLevel);
    }
}
