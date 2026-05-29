package com.farcr.nomansland.client;

import com.farcr.nomansland.NoMansLand;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;

import java.util.Locale;
import java.util.function.Consumer;

/**
 * Disable custom shaders with unsupported graphics
 */
public final class GraphicsCompat {
    private static Boolean customShadersSupported;

    private GraphicsCompat() {}

    public static boolean customShadersSupported() {
        if (customShadersSupported == null) {
            customShadersSupported = computeSupport();
            if (!customShadersSupported) {
                NoMansLand.LOGGER.warn(
                    "No Man's Land custom shaders are disabled on this GPU because its OpenGL driver can "
                        + "crash while compiling them. Effects such as moonlight, sun dogs, the "
                        + "upper atmosphere and the dream sky will not render. Override with "
                        + "-Dnomansland.customShaders=true if you want to force them on."
                );
            }
        }
        return customShadersSupported;
    }

    private static boolean computeSupport() {
        String override = System.getProperty("nomansland.customShaders");
        if (override != null) return Boolean.parseBoolean(override);

        try {
            GLCapabilities caps = GL.getCapabilities();

            if (caps == null || !caps.OpenGL32) {
                NoMansLand.LOGGER.warn(
                    "GPU does not report OpenGL 3.2 support, disabling No Man's Land custom shaders."
                );
                return false;
            }

            if (isCrashProneIntel(caps)) {
                NoMansLand.LOGGER.warn(
                    "Detected legacy Intel integrated graphics whose OpenGL driver crashes while compiling "
                        + "custom shaders, disabling them. Override with -Dnomansland.customShaders=true."
                );
                return false;
            }
        } catch (Throwable t) {
            NoMansLand.LOGGER.warn(
                "Could not query OpenGL capabilities for No Man's Land shaders, "
                    + "assuming custom shaders are supported.", t
            );
        }

        return true;
    }

    private static boolean isCrashProneIntel(GLCapabilities caps) {
        String vendor = stringOrEmpty(GL11.GL_VENDOR);
        String renderer = stringOrEmpty(GL11.GL_RENDERER);

        // cannot believe i am doing this
        boolean intel = vendor.contains("intel") || renderer.contains("intel");
        if (!intel) return false;

        boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
        if (windows && !caps.OpenGL46) return true;

        return renderer.contains("hd graphics") && !renderer.contains("uhd");
    }

    public static void tryRegister(RegisterShadersEvent event, String name, VertexFormat format, Consumer<ShaderInstance> assign) {
        ShaderInstance shader = tryCompile(event, NoMansLand.location(name), format, name);
        if (shader != null) {
            event.registerShader(shader, assign);
        } else {
            NoMansLand.LOGGER.warn(
                "No working shader for '{}' on this GPU ({} / {}); its effect will not render.",
                name, stringOrEmpty(GL11.GL_VENDOR), stringOrEmpty(GL11.GL_RENDERER)
            );
        }
    }

    private static ShaderInstance tryCompile(RegisterShadersEvent event, ResourceLocation id, VertexFormat format, String label) {
        try {
            return new ShaderInstance(event.getResourceProvider(), id, format);
        } catch (Throwable t) {
            NoMansLand.LOGGER.warn("GPU rejected No Man's Land shader '{}', skipping.", label, t);
            return null;
        }
    }

    private static String stringOrEmpty(int name) {
        String value = GL11.glGetString(name);
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
