# Stellar View compatibility (Friend Moon)

This documents the built-in [Stellar View](https://modrinth.com/mod/stellarview)
compatibility for the Friend Moon, and how it stays inert when Stellar View is not
installed. It's written so it can double as a pull-request description.

## The problem

The Friend Moon is drawn by `client.LevelRendererMixin`, which injects into vanilla
`LevelRenderer#renderSky` (fog at `getRainLevel`, the moon after the 3rd
`drawWithShader`, stencil cleanup at `TAIL`).

Stellar View replaces the overworld sky by registering a `DimensionSpecialEffects`
whose `renderSky` returns `true`. In NeoForge, a `true` return makes the patched
`LevelRenderer#renderSky` **return early**, before the vanilla body. Every Friend
Moon injection point sits below that early return, so with Stellar View installed the
moon's state machine still ticks (it's gated on `skyType == NORMAL`, which the
overworld still is) but it is never drawn.

## The fix

A new client mixin, `client.integration.StellarViewViewCenterMixin`, re-runs the
Friend Moon render inside Stellar View's own sky pass.

It targets `net.povstalec.stellarview.client.resourcepack.ViewCenter#renderSkyObjectsFrom`
— the method that draws every Stellar View celestial body (`viewObject.renderFrom(...)`):

- **`@At("HEAD")`** — before any star/planet is drawn: set up the GL state our
  `LevelRendererMixin` normally inherits from the vanilla sky pass, then call
  `FriendMoonRenderer.renderFriendMoonFog` → `renderFriendMoon` → `renderFriendShadow`.
  `renderFriendMoon` lays down the moon's stencil mask, so the Stellar View objects
  drawn afterwards are clipped out of the moon's face (Stellar View uses no stencil of
  its own, so the mask survives). It uses the raw frustum matrix (`modelViewMatrix`,
  param 3) so the moon tracks the player's view exactly as in vanilla.
- **`@At("RETURN")`** — after Stellar View's objects are drawn: `renderFinalize` tears
  the stencil back down and restores depth-mask.

Two details that matter:

- The meeting-point fog (`renderFriendMoonFog`) doesn't set its own blend mode; it's
  pinned to `applySkyBlendFunction()` (the additive "sky" blend it was written for),
  otherwise it inherits whatever blend Stellar View last used and flickers the sky to
  black on alternating frames.
- The moon's world-darkening / light-map effect is unaffected — that runs through
  `client.LightTextureMixin`, not `renderSky`.

## Why it's safe without Stellar View

The mixin is annotated `@IfModPresent("stellarview")`, so `NMLMixinPlugin` only applies
it when `stellarview` is loaded. When it's absent, `shouldApplyMixin` returns `false`,
the mixin is never applied, and the `ViewCenter` class is never referenced — no load
error, no behaviour change.

`client.LevelRendererMixin` is left exactly as-is and the two paths are mutually
exclusive for the Friend Moon:

| State | `LevelRendererMixin` (vanilla `renderSky`) | This mixin (`renderSkyObjectsFrom`) |
|---|---|---|
| Stellar View absent | draws the moon | not applied |
| Stellar View present, sky replacement **off** | draws the moon (vanilla `renderSky` runs) | not reached |
| Stellar View present, sky replacement **on** | hooks skipped (early return) | draws the moon |

So the moon is drawn by exactly one path in every configuration — never zero, never two.

## Files changed

- **new** `src/main/java/com/farcr/nomansland/common/mixin/client/integration/StellarViewViewCenterMixin.java`
- `src/main/resources/nomansland.mixins.json` — added `client.integration.StellarViewViewCenterMixin` to the `client` array
- `src/main/resources/META-INF/neoforge.mods.toml` — added `stellarview` as an `optional`, `CLIENT`-side dependency
- `build.gradle.kts` — added a `compileOnly` Stellar View dependency
- `.gitignore` — added `libs` (so a local Stellar View jar isn't committed)

## Dependency / build note

Stellar View is needed **at compile time only** (`compileOnly`) — it's never bundled
or required at runtime. The committed `build.gradle.kts` currently points at a local
jar so it builds out of the box:

```kotlin
compileOnly(files("libs/Stellar_View-1.21.1-0.5.2-NeoForge.jar"))
```

For upstream, switch to a maven coordinate (and drop `libs/`). Pick one:

```kotlin
// Modrinth (recommended). The version NUMBER "0.5.2" is shared across loaders/MC
// versions, so use the specific Version ID for the 1.21.1 NeoForge build — find it at
// https://modrinth.com/mod/stellarview/versions  ->  Metadata > Version ID.
compileOnly("maven.modrinth:stellarview:<VERSION_ID>")

// CurseForge (1.21.1 NeoForge 0.5.2). Note: CurseMaven can't fetch mods whose author
// disabled third-party sharing, so confirm it resolves before relying on it.
compileOnly("curse.maven:stellarview-865273:7177905")
```

Both the Modrinth and CurseMaven repositories are already configured in
`build.gradle.kts`.

## Testing matrix

Build a client and verify each row of the table above:

1. **No Stellar View** — Friend Moon appears via the vanilla path; no crash on load.
2. **Stellar View, replacement on** — Friend Moon appears over Stellar View's sky,
   with stars/planets clipped out of its face and no flicker.
3. **Stellar View, replacement off** (its config) — Friend Moon appears via the vanilla
   path and is not double-drawn.

## Coupling note for maintainers

The one coupling point is the name/signature of
`ViewCenter#renderSkyObjectsFrom(ClientLevel, Camera, float, Matrix4f, Matrix4f, Runnable, Tesselator)`.
If a future Stellar View release renames or reshapes it, the `@At` targets need
updating. Worth a heads-up between the teams so a Stellar View refactor doesn't
silently no-op the hook.

---

### Suggested PR title

`Add Stellar View compatibility for the Friend Moon`

### Suggested PR description

> Stellar View takes over overworld sky rendering via a `DimensionSpecialEffects` that
> returns `true` from `renderSky`, which early-returns NeoForge's `LevelRenderer#renderSky`
> before our `LevelRendererMixin` Friend Moon hooks can run — so the moon never shows
> with Stellar View installed.
>
> This adds `StellarViewViewCenterMixin`, a `@IfModPresent("stellarview")`-gated client
> mixin that redraws the Friend Moon at the head of Stellar View's
> `ViewCenter#renderSkyObjectsFrom` (so its stencil mask is set before Stellar View's
> stars/planets and clips them off the moon's face) and finalizes the stencil at the
> method's return. Stellar View is a `compileOnly`/`optional` dependency; with it absent
> the mixin isn't applied and behaviour is unchanged. The existing vanilla-path
> `LevelRendererMixin` is untouched, and the two paths are mutually exclusive for the
> moon in every configuration (no double-draw, no missing moon).
