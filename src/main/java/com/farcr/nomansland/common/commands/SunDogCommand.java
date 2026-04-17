package com.farcr.nomansland.common.commands;

import com.farcr.nomansland.common.worldevent.SunDog;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class SunDogCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sundog")
            .requires(stack -> stack.hasPermission(2))
                .executes(SunDogCommand::toggle)
        );
    }

    private static int toggle(CommandContext<CommandSourceStack> context) {
        ServerLevel level = context.getSource().getLevel();
        SunDog sunDog = SunDog.getOrDefault(level);
        if (sunDog.isActive()) {
            sunDog.end();
            context.getSource().sendSuccess(() -> Component.literal("Sun dog ended"), true);
        } else {
            sunDog.forceStart();
            context.getSource().sendSuccess(() -> Component.literal("Sun dog started"), true);
        }
        return 1;
    }
}
