#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PreviousSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform float zoomOut;
uniform float fadeOut;

out vec4 fragColor;

void main() {
    vec2 previousCoord = ((texCoord - vec2(.5)) * zoomOut) + vec2(.5);
    vec4 previousDiffuse = texture(PreviousSampler, previousCoord);

    vec3 compositeColor = (texture(DiffuseSampler, texCoord).rgb * vec3(1. - fadeOut)) + (previousDiffuse.rgb * vec3(fadeOut));
    fragColor = vec4(compositeColor, 1.0);
}
