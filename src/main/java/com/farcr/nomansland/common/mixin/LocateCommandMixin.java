package com.farcr.nomansland.common.mixin;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.friend.FriendMoon;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(LocateCommand.class)
public class LocateCommandMixin {
    @Unique private static final ResourceLocation MEETING_POINT = NoMansLand.location("meeting_point");

    @WrapOperation(method = "locateStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/ChunkGenerator;findNearestMapStructure(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/HolderSet;Lnet/minecraft/core/BlockPos;IZ)Lcom/mojang/datafixers/util/Pair;"))
    private static Pair<BlockPos, Holder<Structure>> nml$locateMeetingPoint(ChunkGenerator generator, ServerLevel level, HolderSet<Structure> structures, BlockPos origin, int searchRadius, boolean skipKnownStructures, Operation<Pair<BlockPos, Holder<Structure>>> original) {
        Pair<BlockPos, Holder<Structure>> found = original.call(generator, level, structures, origin, searchRadius, skipKnownStructures);
        if (found != null) return found;

        Optional<Holder<Structure>> meetingPoint = structures.stream().filter((holder) -> holder.is(MEETING_POINT)).findFirst();
        if (meetingPoint.isEmpty()) return null;

        BlockPos meetingPointPosition = FriendMoon.getMeetingPointPosition(level);
        if (meetingPointPosition == null) return null;

        return Pair.of(meetingPointPosition, meetingPoint.get());
    }
}
