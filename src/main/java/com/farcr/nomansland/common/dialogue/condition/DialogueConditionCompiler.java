package com.farcr.nomansland.common.dialogue.condition;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DialogueConditionCompiler implements PreparableReloadListener {
    private RegistryAccess registryAccess;
    public DialogueConditionCompiler(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    @Override
    public CompletableFuture<Void> reload(
        PreparationBarrier preparationBarrier, ResourceManager resourceManager,
        ProfilerFiller profilerFiller, ProfilerFiller profilerFiller1, Executor executor, Executor executor1
    ) {
        return CompletableFuture.runAsync(() -> {
                MoonlightOfferingConditions.ItemOfferingConditional.COMPILED_MAP.clear();
                MoonlightOfferingConditions.EntityOfferingConditional.COMPILED_MAP.clear();
            }, executor)
            .thenCompose(preparationBarrier::wait)
            .thenAcceptAsync((data) -> {
                Registry<DialogueRegistry.DialoguePool> dialoguePools =
                    registryAccess.registryOrThrow(NMLRegistries.OFFERING_DIALOGUE_KEY);
                dialoguePools.forEach((dialoguePool) -> {
                    if (dialoguePool.condition().isPresent()) {
                        DialogueRegistry.DialogueCondition condition = dialoguePool.condition().get();
                        if (condition instanceof DialogueRegistry.CompiledCondition<?> compiledCondition)
                            compiledCondition.consume(dialoguePool);
                    }
                });

                NoMansLand.LOGGER.info(MoonlightOfferingConditions.ItemOfferingConditional.COMPILED_MAP);
        }, executor1);
    }
}
