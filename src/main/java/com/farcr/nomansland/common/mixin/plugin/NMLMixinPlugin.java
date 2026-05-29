package com.farcr.nomansland.common.mixin.plugin;

import com.farcr.nomansland.common.mixin.plugin.annotation.*;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NMLMixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("No Man's Land Mixin Plugin");
    private static final boolean IS_DEV = !FMLLoader.isProduction();

    private Set<String> presentMods;
    private String mixinPackage;

    @Override
    public void onLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;
        this.presentMods = FMLLoader.getLoadingModList().getMods().stream()
                .map(ModInfo::getModId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (this.mixinPackage != null && !mixinClassName.startsWith(this.mixinPackage)) {
            return true;
        }

        try {
            ClassNode mixinClass = MixinService.getService().getBytecodeProvider().getClassNode(mixinClassName);
            return evaluateConditions(mixinClass, mixinClassName);
        } catch (ClassNotFoundException | IOException e) {
            LOGGER.warn("Could not load mixin class for condition evaluation: {}", mixinClassName);
            return true;
        }
    }

    private boolean evaluateConditions(ClassNode mixinClass, String mixinClassName) {
        boolean hasDevAnnotation = false;
        boolean hasNotDevAnnotation = false;
        if (mixinClass.visibleAnnotations != null) {
            for (AnnotationNode node : mixinClass.visibleAnnotations) {

                if (node.desc.equals(Type.getDescriptor(IfDevEnvironment.class))) {
                    hasDevAnnotation = true;
                    continue;
                }

                if (node.desc.equals(Type.getDescriptor(IfNotDevEnvironment.class))) {
                    hasNotDevAnnotation = true;
                    continue;
                }

                if (node.desc.equals(Type.getDescriptor(IfModPresent.class))) {
                    if (!checkModPresent(node, mixinClassName)) return false;
                    continue;
                }

                if (node.desc.equals(Type.getDescriptor(IfModPresent.List.class))) {
                    List<AnnotationNode> nested = getAnnotationValue(node, "value", Collections.emptyList());
                    for (AnnotationNode inner : nested) {
                        if (!checkModPresent(inner, mixinClassName)) return false;
                    }
                    continue;
                }

                if (node.desc.equals(Type.getDescriptor(IfModAbsent.class))) {
                    if (!checkModAbsent(node, mixinClassName)) return false;
                    continue;
                }

                if (node.desc.equals(Type.getDescriptor(IfModAbsent.List.class))) {
                    List<AnnotationNode> nested = getAnnotationValue(node, "value", Collections.emptyList());
                    for (AnnotationNode inner : nested) {
                        if (!checkModAbsent(inner, mixinClassName)) return false;
                    }
                    continue;
                }

                if (node.desc.equals(Type.getDescriptor(IfAnyModPresent.class))) {
                    String[] value = getAnnotationValue(node, "value", new String[0]);
                    if (value.length == 0) throw new IllegalArgumentException("array of modids must not be empty");
                    boolean anyPresent = false;
                    for (String modId : value) {
                        if (presentMods.contains(modId)) {
                            anyPresent = true;
                            break;
                        }
                    }
                    if (!anyPresent) {
                        LOGGER.debug("Skipping mixin {}; none of {} are present", mixinClassName, List.of(value));
                        return false;
                    }
                }
            }
        }

        if (hasDevAnnotation && hasNotDevAnnotation) {
            LOGGER.warn("Mixin {} has both @IfDevEnvironment and @IfNotDevEnvironment. It will never load!", mixinClassName);
            return false;
        }

        if (hasDevAnnotation && !IS_DEV) {
            LOGGER.debug("Skipping dev-only mixin: {}", mixinClassName);
            return false;
        }

        if (hasNotDevAnnotation && IS_DEV) {
            LOGGER.debug("Skipping production-only mixin: {}", mixinClassName);
            return false;
        }

        return true;
    }

    private boolean checkModPresent(AnnotationNode node, String mixinClassName) {
        String value = getAnnotationValue(node, "value", "");
        if (value.isEmpty()) throw new IllegalArgumentException("modid must not be empty");
        if (!presentMods.contains(value)) {
            LOGGER.debug("Skipping mixin {}; mod '{}' is not present", mixinClassName, value);
            return false;
        }
        return true;
    }

    private boolean checkModAbsent(AnnotationNode node, String mixinClassName) {
        String value = getAnnotationValue(node, "value", "");
        if (value.isEmpty()) throw new IllegalArgumentException("modid must not be empty");
        if (presentMods.contains(value)) {
            LOGGER.debug("Skipping mixin {}; mod '{}' is not absent", mixinClassName, value);
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getAnnotationValue(AnnotationNode annotation, String key, T defaultValue) {
        if (annotation.values == null) return defaultValue;

        for (int i = 0; i < annotation.values.size(); i += 2) {
            if (key.equals(annotation.values.get(i))) {
                return (T) annotation.values.get(i + 1);
            }
        }

        return defaultValue;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return Collections.emptyList();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
