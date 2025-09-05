package com.farcr.nomansland.common.registry.blocks;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.block.torches.ExtinguishableBlock;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NMLExtinguishables {

    public static final DeferredRegister<ExtinguishableBlock> EXTINGUISHABLES = DeferredRegister.create(NMLRegistries.EXTINGUISHABLE_BLOCKS_KEY, NoMansLand.MODID);
    private static boolean registered = false;

    public static <B extends Block> DeferredHolder<ExtinguishableBlock, ExtinguishableBlock> register(String name, Holder<B> from, Holder<B> to) {
        return EXTINGUISHABLES.register(name, () -> new ExtinguishableBlock(from.value(), to.value()));
    }

    static {
        register();
    }

    public static void register() {
        if (!registered) {
            registered = true;

            register("torch", Holder.direct(Blocks.TORCH), NMLBlocks.EXTINGUISHED_TORCH.getDelegate());
            register("torch_wall", Holder.direct(Blocks.WALL_TORCH), NMLBlocks.EXTINGUISHED_WALL_TORCH.getDelegate());
            register("soul_torch", Holder.direct(Blocks.SOUL_TORCH), NMLBlocks.EXTINGUISHED_SOUL_TORCH.getDelegate());
            register("soul_wall_torch", Holder.direct(Blocks.SOUL_WALL_TORCH), NMLBlocks.EXTINGUISHED_SOUL_WALL_TORCH.getDelegate());

            register("sconce_torch", NMLBlocks.SCONCE_TORCH.getDelegate(), NMLBlocks.EXTINGUISHED_SCONCE_TORCH.getDelegate());
            register("sconce_wall_torch", NMLBlocks.SCONCE_WALL_TORCH.getDelegate(), NMLBlocks.EXTINGUISHED_SCONCE_WALL_TORCH.getDelegate());
            register("soul_sconce_torch", NMLBlocks.SCONCE_SOUL_TORCH.getDelegate(), NMLBlocks.EXTINGUISHED_SCONCE_SOUL_TORCH.getDelegate());
            register("soul_sconce_wall_torch", NMLBlocks.SCONCE_SOUL_WALL_TORCH.getDelegate(), NMLBlocks.EXTINGUISHED_SCONCE_SOUL_WALL_TORCH.getDelegate());
        } else {
            throw new IllegalStateException("Unable to register Extinguishables; Already registered!");
        }
    }
}
