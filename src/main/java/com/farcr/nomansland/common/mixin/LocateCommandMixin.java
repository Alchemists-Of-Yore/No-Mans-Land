package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.friend.FriendMoon;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceOrTagKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocateCommand.class)
public class LocateCommandMixin {
    @Unique
    private static final ResourceLocation MEETING_POINT = NoMansLand.location("meeting_point");

    @Shadow private static float dist(int x1, int z1, int x2, int z2) {
        throw new NotImplementedException();
    }

    @Inject(method = "locateStructure", at = @At("HEAD"), cancellable = true)
    private static void nml$locateStructure(
        CommandSourceStack source, ResourceOrTagKeyArgument.Result<Structure> structure,
        CallbackInfoReturnable<Integer> cir
    ) {
        structure.unwrap().left().ifPresent((key) -> {
            if (key.location().equals(MEETING_POINT)) {
                ServerLevel serverlevel = source.getLevel();
                BlockPos blockPos = FriendMoon.getMeetingPointPosition(serverlevel);
                if (blockPos == null)
                    return;

                BlockPos sourcePosition = BlockPos.containing(source.getPosition());
                int i = Mth.floor(dist(sourcePosition.getX(), sourcePosition.getZ(), blockPos.getX(), blockPos.getZ()));
                String s = "~";
                Component component = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates",
                    new Object[]{blockPos.getX(), s, blockPos.getZ()})).withStyle((p_214489_) -> p_214489_.withColor(ChatFormatting.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + blockPos.getX() + " ~ " + blockPos.getZ()))
                    .withHoverEvent(new HoverEvent(net.minecraft.network.chat.HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
                );
                source.sendSuccess(() -> Component.translatable("commands.locate.structure.success", new Object[]{MEETING_POINT.toString(), component, i}), false);
                cir.cancel();
            }
        });
    }
}
