package com.farcr.nomansland.datagen;

import com.farcr.nomansland.NoMansLand;
import com.farcr.nomansland.common.friend.condition.DialogueConditionCompiler;
import com.farcr.nomansland.common.friend.dialogue.DialoguePool;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class NMLDialogueLanguageProvider implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final PackOutput output;
    private final Path existingDataRoot;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;

    public NMLDialogueLanguageProvider(PackOutput output, Path existingDataRoot, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        this.output = output;
        this.existingDataRoot = existingDataRoot;
        this.lookupProvider = lookupProvider;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        return lookupProvider.thenCompose(registries -> {
            RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registries);

            CompletableFuture<?>[] futures = DialogueConditionCompiler.REGISTRIES.stream()
                .map(registryKey -> generateForRegistry(cache, registryKey, registryOps))
                .toArray(CompletableFuture[]::new);

            return CompletableFuture.allOf(futures);
        });
    }

    private CompletableFuture<?> generateForRegistry(CachedOutput cache, ResourceKey<Registry<DialoguePool>> registryKey, RegistryOps<JsonElement> registryOps) {
        String registryPath = registryKey.location().getPath();

        Path dataDir = existingDataRoot.resolve("data")
            .resolve(registryKey.location().getNamespace())
            .resolve(registryKey.location().getNamespace())
            .resolve(registryPath);

        if (!Files.isDirectory(dataDir)) return CompletableFuture.completedFuture(null);

        TreeMap<String, String> entries = new TreeMap<>();

        try (Stream<Path> paths = Files.walk(dataDir)) {
            paths.filter(p -> p.toString().endsWith(".json")).forEach(path -> {
                try {
                    String content = Files.readString(path);
                    JsonElement json = JsonParser.parseString(content);
                    DialoguePool pool = DialoguePool.CODEC.parse(registryOps, json)
                        .getOrThrow(msg -> new RuntimeException("Failed to parse " + path + ": " + msg));

                    String relativePath = dataDir.relativize(path).toString()
                        .replace(".json", "")
                        .replace('\\', '/');
                    String key = relativePath.replace("/", ".");
                    entries.put(key, pool.text());
                } catch (Exception e) {
                    LOGGER.warn("Failed to parse dialogue pool entry {}: {}", path, e.getMessage());
                }
            });
        } catch (IOException e) {
            LOGGER.warn("Failed to read dialogue pool directory {}: {}", dataDir, e.getMessage());
        }

        if (entries.isEmpty()) return CompletableFuture.completedFuture(null);

        JsonObject langJson = new JsonObject();
        entries.forEach(langJson::addProperty);

        Path outputPath = output.getOutputFolder()
            .resolve("assets/" + NoMansLand.MODID + "/lang/nomansland/" + registryPath + "/en_us.json");
        return DataProvider.saveStable(cache, langJson, outputPath);
    }

    @Override
    public String getName() {
        return "NML Dialogue Language";
    }
}
