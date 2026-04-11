package com.farcr.nomansland.common.friend;

import com.farcr.nomansland.common.dreams.DreamManager;
import com.farcr.nomansland.common.dreams.DreamType;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamLevelHandler;
import com.farcr.nomansland.common.dreams.dreamlevel.DreamServerLevel;
import com.farcr.nomansland.common.dreams.dreamtypes.MoonlightDreamType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/* A list of events the client can update the server with */
public class FriendMoonUpdate {
    public enum ToServer {
        AWAKEN(0, (moon, player) -> {
            moon.awake = true;
            moon.wokenUpBy = player;
        }),
        SAW_MOON_IN_DREAM(1, (moon, player) -> {
            DreamManager manager = DreamManager.getOrDefault(player.getServer());
            DreamType dreamType = manager.playerGetDream(player);
            if (manager.playerIsDreaming(player) && dreamType instanceof MoonlightDreamType) {
                // "theres gotta be a better way to do this" -person who wrote the system
                ((MoonlightDreamType.MoonlightDreamTypeInstance)
                    DreamLevelHandler.getDreamLevel(player.getServer(), dreamType, player)
                    .getDreamTypeInstance()).hasSeenMoon();
            }
        });

        private final int id;
        private final BiConsumer<FriendMoon, ServerPlayer> consumer;

        ToServer(int id, BiConsumer<FriendMoon, ServerPlayer> consumer) {
            this.id = id;
            this.consumer = consumer;
        }

        public int getId() { return id;}

        public BiConsumer<FriendMoon, ServerPlayer> getConsumer() { return consumer; }

        public static final IntFunction<ToServer> BY_ID = ByIdMap.continuous(ToServer::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, ToServer> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ToServer::getId);
    }

    public enum ToClient {
        DUMMY(0, (player) -> {});

        private final int id;
        private final Consumer<Player> consumer;

        ToClient(int id, Consumer<Player> consumer) {
            this.id = id;
            this.consumer = consumer;
        }

        public int getId() { return id;}

        public Consumer<Player> getConsumer() { return consumer; }

        public static final IntFunction<ToClient> BY_ID = ByIdMap.continuous(ToClient::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final StreamCodec<ByteBuf, ToClient> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, ToClient::getId);
    }
}