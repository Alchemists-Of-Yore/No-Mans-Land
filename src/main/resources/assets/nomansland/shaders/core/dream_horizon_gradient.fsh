#version 150
#define pi 3.14

in vec3 vertexPosition;

uniform vec4 ColorModulator;

out vec4 fragColor;

void main() {
    float slice = (1. / 6.);
    float alpha = smoothstep(0., slice, vertexPosition.y);
    fragColor = ColorModulator * alpha;
}