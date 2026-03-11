#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform mat4 ProjMat;
uniform vec4 ColorModulator;
uniform vec2 ScreenResolution;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float linearizeDepth(float depthSample) {
    return -ProjMat[3].z /  (depthSample * -2.0 + 1.0 - ProjMat[2].z);
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0) discard;

    float depthSample = texture(Sampler1, gl_FragCoord.xy / ScreenResolution).r;
    float depth = linearizeDepth(depthSample);
    float particleDepth = linearizeDepth(gl_FragCoord.z);

    float opacity = smoothstep(0.0, 32.0, depth - particleDepth);

    fragColor = color * vertexColor * ColorModulator * vec4(1.0, 1.0, 1.0, opacity);
}
