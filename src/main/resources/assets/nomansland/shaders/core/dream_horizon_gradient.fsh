#version 150
#define pi 3.14

in vec3 vertexPosition;

uniform vec4 ColorModulator;
uniform float Slice;

out vec4 fragColor;

void main() {
    float alpha = smoothstep(0., Slice, vertexPosition.y);
    fragColor = ColorModulator * alpha;
}