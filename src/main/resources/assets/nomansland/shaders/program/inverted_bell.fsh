#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform float Fade;

out vec4 fragColor;

void main(){
    vec4 diffuseColor = texture(DiffuseSampler, texCoord);
    vec3 desaturated = vec3(dot(diffuseColor.rgb, vec3(0.299,0.587,0.114)));
    float vignette = length(texCoord - vec2(.5)) * Fade * 2.0;
    fragColor = vec4(mix(diffuseColor.rgb, desaturated, Fade) * (1.0 - vignette) * (1.0 - sqrt(Fade) * 0.5), 1.0);
}
