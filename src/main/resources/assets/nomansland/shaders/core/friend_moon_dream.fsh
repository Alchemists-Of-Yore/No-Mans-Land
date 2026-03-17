#version 150
#define pi 3.14
#define r(a) mat2(cos(a + vec4(0, 33, 11, 0)))

in vec3 vertexPosition;

uniform vec4 ColorModulator;
uniform float Intensity;
uniform float Time;

out vec4 fragColor;

vec3 colorHaze = vec3(168., 143., 87.) / vec3(255.);

// https://thebookofshaders.com/13/
float random (in vec2 st) {
    return fract(sin(dot(st.xy,
    vec2(12.9898,78.233)))*
    43758.5453123);
}

// Based on Morgan McGuire @morgan3d
// https://www.shadertoy.com/view/4dS3Wd
float noise (in vec2 st) {
    vec2 i = floor(st);
    vec2 f = fract(st);

    // Four corners in 2D of a tile
    float a = random(i);
    float b = random(i + vec2(1.0, 0.0));
    float c = random(i + vec2(0.0, 1.0));
    float d = random(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(a, b, u.x) +
    (c - a)* u.y * (1.0 - u.x) +
    (d - b) * u.x * u.y;
}

#define OCTAVES 6
float fbm (in vec2 st) {
    // Initial values
    float value = 0.0;
    float amplitude = .5;
    float frequency = 0.;
    //
    // Loop of octaves
    for (int i = 0; i < OCTAVES; i++) {
        value += amplitude * noise(st);
        st *= 2.;
        amplitude *= .5;
    }
    return value;
}


void main() {
    vec3 adjustedPosition = vec3(vertexPosition);
    adjustedPosition.x += Time / 6.;

    float colorIntensity = 1. + sin(Time * pi) / 32.;
    colorIntensity = max(colorIntensity, 0.01);
    vec4 darkness = vec4(0.0, 0.0, 0.0, 1.0);

    float yIntensity = adjustedPosition.y - ((1. - adjustedPosition.y) / colorIntensity);
    int maxIter = 3;
    for (int i = 0; i < maxIter; i++) {
        float localTime = Time * float(i);
        yIntensity += (.5 + (sin(((adjustedPosition.x + localTime / 12.) * 16.) / pi) / 5.));
        yIntensity += cos(adjustedPosition.x) / float(maxIter * maxIter);
        yIntensity *= abs(adjustedPosition.y);
    }

    float i;
    float stepSize = 0.1;
    vec3 position = vec3(0., adjustedPosition.y, 0.);

    vec3 colorLine = vec3(1.);
    float timeAdjust = Time / 8.;
    for (colorLine *= i; i < 20.0; i++) {
        position.xz += yIntensity + sin(timeAdjust);
        position.xy *= r(-position.y * 0.0001);
        stepSize = max(stepSize, 4.0 * (-length(position.z) + 10.0));
        stepSize += abs(
            cos(position.z) * 0.015 +
            sin(position.x * 0.5) * 0.9 +
            1.0
        );

        position.y -= fbm(adjustedPosition.xz + timeAdjust / 1.) * stepSize;
        colorLine += 1.0 / (stepSize * 0.2) * i;
    }
    colorLine /= 0.5e2;
    colorLine = tanh(colorLine + colorLine);
    colorLine = clamp(colorLine, 0.1, 1.);
    vec2 xz = vec2(vertexPosition.x, vertexPosition.z);

    float minValue = .1;
    colorLine *= clamp(fbm(xz - timeAdjust), minValue, min(.75 * sqrt(adjustedPosition.y / yIntensity), .75));

    darkness.xyz += yIntensity * colorHaze;
    darkness = min(darkness, vec4(colorHaze, 1.0));
    fragColor = darkness * vec4(colorLine, 1.);
}