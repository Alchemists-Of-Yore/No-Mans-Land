package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.structure.bell_sanctuary.BellSanctuaryStructure;
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

    public static final Supplier<StructureType<BellSanctuaryStructure>> BELL_SANCTUARY = register("bell_sanctuary", BellSanctuaryStructure.MAP_CODEC);

    private static <P extends Structure> DeferredHolder<StructureType<?>, StructureType<P>> register (String name, MapCodec<P> codec) {
        return STRUCTURE_TYPES.register(name, () -> () -> codec);
    }
}
