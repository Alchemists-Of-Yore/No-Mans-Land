package com.farcr.nomansland.common.friend;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum FriendMoonState implements StringRepresentable {
    IDLE("idle"),
    OFFERING("offering");

    private final String name;
    public static final EnumCodec<FriendMoonState> CODEC = StringRepresentable.fromEnum(FriendMoonState::values);
    FriendMoonState(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
