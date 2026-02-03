package com.farcr.nomansland.common.friend.condition;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.friend.dialogue.DialogueRegistry;
import com.farcr.nomansland.common.registry.NMLRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DialogueConditionCompiler implements PreparableReloadListener {
    private RegistryAccess registryAccess;
    public DialogueConditionCompiler(RegistryAccess registryAccess) {
        this.registryAccess = registryAccess;
    }

    public static final List<ResourceKey<Registry<DialogueRegistry.DialoguePool>>> REGISTRIES = List.of(
        NMLRegistries.GREETING_DIALOGUE_KEY,
        NMLRegistries.PASSIVE_DIALOGUE_KEY,
        NMLRegistries.OFFERING_DIALOGUE_KEY,
        NMLRegistries.NEGATIVE_DIALOGUE_KEY,
        NMLRegistries.CONTEXTUAL_DIALOGUE_KEY
    );

    @Override
    public CompletableFuture<Void> reload(
        PreparationBarrier preparationBarrier, ResourceManager resourceManager,
        ProfilerFiller profilerFiller, ProfilerFiller profilerFiller1, Executor executor, Executor executor1
    ) {
        return CompletableFuture.runAsync(() -> {
                // Offering Conditions
                MoonlightOfferingConditions.ItemOfferingConditional.COMPILED_MAP.clear();
                MoonlightOfferingConditions.EntityOfferingConditional.COMPILED_MAP.clear();
                // Greeting Conditions
                MoonlightGreetingConditions.FirstTimeGreetingConditional.FIRST_TIME_ARRAY.clear();
            }, executor)
            .thenCompose(preparationBarrier::wait)
            .thenAcceptAsync((data) -> {
                for (ResourceKey<Registry<DialogueRegistry.DialoguePool>> registryKey : REGISTRIES) {
                    Registry<DialogueRegistry.DialoguePool> dialoguePools =
                        registryAccess.registryOrThrow(registryKey);
                    dialoguePools.forEach((dialoguePool) -> {
                        if (dialoguePool.condition().isPresent() && dialoguePool.condition().get().validate(registryKey)) {
                            DialogueRegistry.DialogueCondition condition = dialoguePool.condition().get();
                            if (condition instanceof DialogueRegistry.CompiledCondition<?> compiledCondition)
                                compiledCondition.consume(dialoguePool);
                            if (condition instanceof DialogueRegistry.ListCondition listCondition)
                                listCondition.append(dialoguePool);
                        }
                    });
                }
        }, executor1);
    }
}
