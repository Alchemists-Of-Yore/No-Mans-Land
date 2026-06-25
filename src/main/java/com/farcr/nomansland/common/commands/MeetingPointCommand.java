package com.farcr.nomansland.common.commands;

import com.farcr.nomansland.common.extension.ChunkGeneratorStructureStateExtension;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MeetingPointCommand {
    private static final String MEETING_POINT_ID = "nomansland:meeting_point";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("meetingpoint")
            .requires(stack -> stack.hasPermission(2))
                .executes(MeetingPointCommand::query)
                .then(Commands.literal("set")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context -> set(context, BlockPosArgument.getLoadedBlockPos(context, "pos")))))
                .then(Commands.literal("reset")
                    .executes(MeetingPointCommand::reset))
        );
    }

    private static int query(CommandContext<CommandSourceStack> context) {
        BlockPos current = FriendMoon.getMeetingPointPosition(context.getSource().getLevel());
        if (current == null) {
            context.getSource().sendSuccess(() -> Component.literal("No meeting point exists in this world"), false);
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.literal(
            "Meeting point is at " + current.getX() + ", " + current.getZ()), false);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        apply(context.getSource().getServer(), chunkPos);

        BlockPos center = chunkPos.getMiddleBlockPosition(0);
        context.getSource().sendSuccess(() -> Component.literal(
            "Meeting point moved to " + center.getX() + ", " + center.getZ()), true);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        ServerLevel overworld = server.overworld();
        ChunkPos canonical = resolveCanonicalPosition(overworld, extension(overworld));
        apply(server, canonical);
        context.getSource().sendSuccess(() -> Component.literal("Meeting point reset to its generated position"), true);
        return 1;
    }

    private static void apply(MinecraftServer server, @Nullable ChunkPos pos) {
        ServerLevel overworld = server.overworld();
        FriendMoon.getOrDefault(overworld).setMeetingPointOverride(pos);
        extension(overworld).nomansland$setMeetingPointPosition(pos);
        refreshClients(overworld);
    }

    public static void initializeMeetingPoint(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        FriendMoon moon = FriendMoon.getOrDefault(overworld);
        ChunkGeneratorStructureStateExtension extension = extension(overworld);

        if (!moon.hasMigratedMeetingPoint()) {
            if (moon.getMeetingPointOverride() == null) {
                ChunkPos canonical = resolveCanonicalPosition(overworld, extension);
                if (canonical != null) {
                    moon.setMeetingPointOverride(canonical);
                }
            }
            moon.setMeetingPointMigrated(true);
        }

        ChunkPos override = moon.getMeetingPointOverride();
        if (override != null) {
            extension.nomansland$setMeetingPointPosition(override);
        }
    }

    private static ChunkPos resolveCanonicalPosition(ServerLevel overworld, ChunkGeneratorStructureStateExtension extension) {
        ChunkPos legacy = extension.nomansland$computeLegacyMeetingPointPosition();
        if (legacy != null && meetingPointGeneratedAt(overworld, legacy)) {
            return legacy;
        }
        return extension.nomansland$generatedMeetingPointPosition();
    }

    private static boolean meetingPointGeneratedAt(ServerLevel overworld, ChunkPos chunkPos) {
        try {
            Optional<CompoundTag> data = overworld.getChunkSource().chunkMap.read(chunkPos).join();
            if (data.isEmpty()) return false;
            CompoundTag starts = data.get().getCompound("structures").getCompound("starts");
            CompoundTag start = starts.getCompound(MEETING_POINT_ID);
            return !start.isEmpty() && !StructureStart.INVALID_START_ID.equals(start.getString("id"));
        } catch (Exception exception) {
            return true;
        }
    }

    private static ChunkGeneratorStructureStateExtension extension(ServerLevel level) {
        ChunkGeneratorStructureState state = level.getChunkSource().getGeneratorState();
        return (ChunkGeneratorStructureStateExtension) state;
    }

    private static void refreshClients(ServerLevel overworld) {
        FriendMoon friendMoon = FriendMoon.getOrDefault(overworld);
        overworld.players().forEach(friendMoon::playerSendShadowPacket);
    }
}
