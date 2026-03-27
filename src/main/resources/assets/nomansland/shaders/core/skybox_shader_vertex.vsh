#version 150

in vec3 Position;
in vec4 Color;

uniform vec4 SkyColor;
uniform vec4 FogColor;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec3 vertexPosition;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexPosition = vec3(Position.xyz);
}