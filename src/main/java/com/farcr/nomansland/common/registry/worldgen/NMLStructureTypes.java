package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.structure.AlchemistRuinsStructure;
import com.farcr.nomansland.common.world.structure.DreamMeetingPoint;
import com.farcr.nomansland.common.world.structure.SurfaceJigsawStructure;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NMLStructureTypes {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, NoMansLand.MODID);

//    public static final Supplier<StructureType<CaveStructure>> CAVE = register("cave", CaveStructure.CODEC);
    public static final Supplier<StructureType<AlchemistRuinsStructure>> ALCHEMIST_RUINS = register("alchemist_ruins", AlchemistRuinsStructure.CODEC);
    public static final Supplier<StructureType<DreamMeetingPoint>> DREAM_MEETING_POINT = register("dream_meeting_point", DreamMeetingPoint.CODEC);
    public static final Supplier<StructureType<SurfaceJigsawStructure>> SURFACE_JIGSAW = register("surface_jigsaw", SurfaceJigsawStructure.CODEC);

    private static <P extends Structure> DeferredHolder<StructureType<?>, StructureType<P>> register (String name, MapCodec<P> codec) {
        return STRUCTURE_TYPES.register(name, () -> () -> codec);
    }
}
