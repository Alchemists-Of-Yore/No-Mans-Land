#version 150
#define PI 3.14159265
#define TAU 6.28318531

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform float ElapsedTime;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

// Based on Morgan McGuire @morgan3d
// https://www.shadertoy.com/view/4dS3Wd
// https://graphicscodex.com
float Hash(float p) {
    p = fract(p * 0.011);
    p *= p + 7.5;
    p *= p + p;
    return fract(p);
}

// Based on Morgan McGuire @morgan3d
// https://www.shadertoy.com/view/4dS3Wd
// https://graphicscodex.com
float Noise(vec3 x) {
    const vec3 step = vec3(110.0, 241.0, 171.0);

    vec3 i = floor(x);
    vec3 f = fract(x);

    // For performance, compute the base input to a 1D hash from the integer part of the argument and the
    // incremental change to the 1D based on the 3D -> 1D wrapping
    float n = dot(i, step);

    vec3 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(mix(Hash(n + dot(step, vec3(0, 0, 0))), Hash(n + dot(step, vec3(1, 0, 0))), u.x), mix(Hash(n + dot(step, vec3(0, 1, 0))), Hash(n + dot(step, vec3(1, 1, 0))), u.x), u.y), mix(mix(Hash(n + dot(step, vec3(0, 0, 1))), Hash(n + dot(step, vec3(1, 0, 1))), u.x), mix(Hash(n + dot(step, vec3(0, 1, 1))), Hash(n + dot(step, vec3(1, 1, 1))), u.x), u.y), u.z);
}

// Based on Morgan McGuire @morgan3d
// https://www.shadertoy.com/view/4dS3Wd
// https://graphicscodex.com
float FBM(vec3 x) {
    float v = 0.0;
    float a = 0.5;
    vec3 shift = vec3(100, 100, 100);

    for(int i = 0; i < 3; ++i) {
        v += a * Noise(x);
        x = x * 2.0 + shift;
        a *= 0.5;
    }

    return v;
}

void main() {
    float time = ElapsedTime / 10.0;
    vec2 uv = texCoord0;
    uv.y *= 0.25;
    vec3 coordinates = vec3(uv, 0);

    // First warp
    // fbm( coordinates )
    float qFbm = FBM(coordinates);
    vec3 q = vec3(qFbm, qFbm, qFbm);

    // Second warp
    // fbm( coordinates + fbm( coordinates ) )
    float rFbm = FBM(coordinates + (0.3 * q) + (2.25 * time));
    vec3 r = vec3(rFbm, rFbm, rFbm);

    // Third warp
    // fbm( coordinates + fbm( coordinates + fbm( coordinates ) ) )
    float s = FBM(coordinates + 4.0 * r);

    // Blend factor
    float warpBlend = clamp(s * s * s + 0.6 * s * s + 0.5 * s, 0.0, 1.0);

    // Fade out horizontal edges
    warpBlend *= abs(sin(uv.x * PI));

    // Fade Bottom edge
    warpBlend *= uv.y * 4.;

    // Blend between palette
    vec3 darkColor = vec3(0.212,0.176,0.267);
    vec3 midColor = vec3(0.420,0.400,0.592);
    vec3 lightColor = vec3(0.776,0.843,1.000);

    vec3 c = vec3(0.0);
    c = mix(c, darkColor, clamp(warpBlend * 3.0, 0.0, 1.0));
    c = mix(c, midColor, clamp(warpBlend * 3.0 - 1.0, 0.0, 1.0));
    c = mix(c, lightColor, clamp(warpBlend * 3.0 - 2.0, 0.0, 1.0));

    fragColor = vec4(c, warpBlend);
}