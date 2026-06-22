package com.farcr.nomansland.common.mixin.client.integration;

import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.common.mixin.plugin.annotation.IfModPresent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.povstalec.stellarview.client.resourcepack.ViewCenter;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stellar View compatibility for the Friend Moon.
 *
 * <p>Stellar View replaces the overworld sky by registering a
 * {@link DimensionSpecialEffects} whose {@code renderSky} returns {@code true},
 * which makes NeoForge's patched {@code LevelRenderer#renderSky} return early.
 * Every injection point our {@code client.LevelRendererMixin} uses to draw the
 * Friend Moon sits below that early return, so with Stellar View installed the
 * moon's logic still ticks but it is never drawn.
 *
 * <p>This mixin re-runs the Friend Moon render inside Stellar View's own sky pass.
 * {@code ViewCenter#renderSkyObjectsFrom} is the method that draws every Stellar
 * View celestial body ({@code viewObject.renderFrom(...)}); by rendering the moon
 * at its HEAD we draw the moon and lay down its stencil mask BEFORE Stellar View
 * draws its stars/planets, so those are clipped out of the moon's face exactly as
 * the vanilla path intends (Stellar View uses no stencil of its own, so the mask
 * survives its draws). We tear the stencil back down at the method's RETURN.
 *
 * <p>Mutual exclusivity with {@code client.LevelRendererMixin}:
 * <ul>
 *   <li>Stellar View absent, or its sky replacement disabled - vanilla
 *       {@code renderSky} runs, {@code LevelRendererMixin} draws the moon, and
 *       {@code renderSkyObjectsFrom} is never reached so this mixin does nothing.</li>
 *   <li>Stellar View replacing the sky - vanilla {@code renderSky} early-returns,
 *       {@code LevelRendererMixin}'s moon hooks never fire, and this mixin draws it.</li>
 * </ul>
 * The two paths can never both draw the moon in the same frame.
 *
 * <p>Gated with {@link IfModPresent} so the No Man's Land Mixin Plugin only applies
 * it when {@code stellarview} is loaded; when it is absent the {@code ViewCenter}
 * class is never referenced and nothing changes.
 */
@IfModPresent("stellarview")
@Mixin(ViewCenter.class)
public abstract class StellarViewViewCenterMixin {

    @Inject(method = "renderSkyObjectsFrom", at = @At("HEAD"))
    private void nml$renderFriendMoonBeforeStellarObjects(
            ClientLevel level, Camera camera, float partialTicks,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            Runnable setupFog, Tesselator tesselator,
            CallbackInfoReturnable<Boolean> cir) {

        // Friend Moon only exists in overworld-like skies (same gate as
        // FriendMoonRenderer#tickClientState). Stellar View's nether effects use
        // SkyType.NONE and its end effects use SkyType.END.
        if (level.effects().skyType() != DimensionSpecialEffects.SkyType.NORMAL) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        FriendMoonRenderer renderer = FriendMoonRenderer.getInstance();

        // Preserve the shader colour Stellar View set for its objects.
        float[] prevColor = RenderSystem.getShaderColor().clone();

        // Re-create the GL state our LevelRenderer injections normally inherit from
        // the vanilla sky pass: POSITION_TEX shader (the moon/stars draw through
        // BufferUploader.drawWithShader, which uses the current shader), blending on,
        // depth writes off.
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // renderFriendMoonFog does not set its own blend mode; pin it to the additive
        // "sky" blend it expects, otherwise it inherits Stellar View's last-used mode
        // (which varies per frame) and the fog hemisphere flickers the sky to black.
        FriendMoonRenderer.applySkyBlendFunction();

        // fog -> moon (+ buddy stars; sets the stencil mask) -> shadow.
        // renderFinalize is intentionally deferred to the RETURN injection below so
        // the stencil mask stays active while Stellar View draws its objects.
        renderer.renderFriendMoonFog(modelViewMatrix, projectionMatrix, new PoseStack());
        renderer.renderFriendMoon(modelViewMatrix, projectionMatrix, tesselator, new PoseStack(), partialTicks);
        renderer.renderFriendShadow(modelViewMatrix, projectionMatrix, tesselator, new PoseStack(), partialTicks);

        // Hand a clean colour/blend back, but keep depth-mask off and the stencil
        // test active so Stellar View's objects render and get masked correctly.
        RenderSystem.setShaderColor(prevColor[0], prevColor[1], prevColor[2], prevColor[3]);
        RenderSystem.defaultBlendFunc();
    }

    @Inject(method = "renderSkyObjectsFrom", at = @At("RETURN"))
    private void nml$finalizeFriendMoon(
            ClientLevel level, Camera camera, float partialTicks,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            Runnable setupFog, Tesselator tesselator,
            CallbackInfoReturnable<Boolean> cir) {

        if (level.effects().skyType() != DimensionSpecialEffects.SkyType.NORMAL) {
            return;
        }

        // Stellar View has finished drawing its (masked) objects - disable the
        // stencil test and restore state. Safe to call even if nothing was drawn
        // (disableStencil no-ops when no mask was captured).
        FriendMoonRenderer.renderFinalize(modelViewMatrix, projectionMatrix, Tesselator.getInstance(), partialTicks);
        RenderSystem.depthMask(true);
    }
}
