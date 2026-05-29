package com.farcr.nomansland.common.commands;

import com.farcr.nomansland.common.extension.ChunkGeneratorStructureStateExtension;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;

public class MeetingPointCommand {
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

    private static ChunkGeneratorStructureStateExtension extension(CommandSourceStack source) {
        ChunkGeneratorStructureState state = source.getLevel().getChunkSource().getGeneratorState();
        return (ChunkGeneratorStructureStateExtension) state;
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
        CommandSourceStack source = context.getSource();
        ChunkPos chunkPos = new ChunkPos(pos);
        extension(source).nomansland$setMeetingPointPosition(chunkPos);
        refreshClients(source.getLevel());

        BlockPos center = chunkPos.getMiddleBlockPosition(0);
        source.sendSuccess(() -> Component.literal(
            "Meeting point moved to " + center.getX() + ", " + center.getZ()), true);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        extension(source).nomansland$setMeetingPointPosition(null);
        refreshClients(source.getLevel());
        source.sendSuccess(() -> Component.literal("Meeting point reset to its generated position"), true);
        return 1;
    }

    private static void refreshClients(ServerLevel level) {
        FriendMoon friendMoon = FriendMoon.getOrDefault(level.getServer().overworld());
        level.players().forEach(friendMoon::playerSendShadowPacket);
    }
}
