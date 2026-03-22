package com.farcr.nomansland.common.friend;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;
import java.util.function.IntFunction;

/* A list of events the client can update the server with */
public class FriendMoonUpdate {
    public enum ToServer {
        AWAKEN(0, (moon) -> {
            moon.awake = true;
        });

        private final int id;
        private final Consumer<FriendMoon> consumer;

        ToServer(int id, Consumer<FriendMoon> consumer) {
            this.id = id;
            this.consumer = consumer;
        }

        public int getId() { return id;}

        public Consumer<FriendMoon> getConsumer() { return consumer; }

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