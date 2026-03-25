#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform float Fade;

out vec4 fragColor;

void main(){
    vec4 diffuseColor = texture(DiffuseSampler, texCoord);

    float desatValue = min(Fade * 2, 1);
    vec3 col = mix(diffuseColor.rgb, vec3(dot(diffuseColor.rgb, vec3(0.299,0.587,0.114))), desatValue);

    float greyValue = max(Fade * 2 - 1, 0);
    col = mix(col, vec3(0.0), greyValue);

    float vignetteValue = length(texCoord - vec2(.5)) * Fade * 2.0;
    col = col * (1.0 - vignetteValue) * (1.0 - sqrt(Fade) * 0.5);

    fragColor = vec4(col, 1.0);
}
