package com.farcr.nomansland.common.friend;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

import java.util.function.Consumer;
import java.util.function.IntFunction;

/* A list of events the client can update the server with */
public enum FriendMoonUpdate {
    AWAKEN(0, (moon) -> {moon.awake = true;});

    private final int id;
    private final Consumer<FriendMoon> consumer;
    FriendMoonUpdate(int id, Consumer<FriendMoon> consumer) {
        this.id = id;
        this.consumer = consumer;
    }

    public int getId() { return id; }
    public Consumer<FriendMoon> getConsumer() { return consumer; }

    public static final IntFunction<FriendMoonUpdate> BY_ID = ByIdMap.continuous(FriendMoonUpdate::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, FriendMoonUpdate> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, FriendMoonUpdate::getId);
}