package com.farcr.nomansland.common.registry.worldgen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.world.structure.bell_sanctuary.CenterBellPiece;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType.*;

public class NMLStructurePieces {
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, NoMansLand.MODID);

    public static final Supplier<StructurePieceType> CENTRAL_BELL_SANCTUARY_PIECE = setTemplatePieceId(() -> CenterBellPiece::new, "center_bell_piece");


    private static DeferredHolder<StructurePieceType, StructurePieceType> setPieceId(final Supplier<ContextlessType> factory, final String key) {
        return register(factory, key);
    }

    private static DeferredHolder<StructurePieceType, StructurePieceType> setTemplatePieceId(final Supplier<StructureTemplateType> templateType, final String pieceId) {
        return register(templateType, pieceId);
    }

    public static <P extends StructurePieceType> DeferredHolder<StructurePieceType, StructurePieceType> register(final Supplier<P> piece, final String name) {
        return STRUCTURE_PIECES.register(name, piece);
    }
}
