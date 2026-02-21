#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 TextureMat;
uniform int FogShape;

out float vertexDistance;
out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    // 0 = u0, v0
    // 1 = u0, v1
    // 2 = u1, v1
    // 3 = u1, v0
    int faceVertexID = gl_VertexID % 4;
    float u = 1.0 * (faceVertexID / 2);
    float v = 1.0 * clamp((faceVertexID % 3), 0, 1);

    vertexDistance = fog_distance(Position, FogShape);
    texCoord0 = (TextureMat * vec4(u, v, 0.0, 1.0)).xy;
}