#version 330

in vec3 fPos;
in vec3 fNormal;

out vec4 fragColor;

vec4 color = vec4(0.0 / 255, 255.0 / 255, 0.0 / 255, 1.0);

vec4 ambientColor = vec4(1.0, 1.0, 1.0, 1.0);
float ambientIntensity = 0.8;

vec4 diffuseColor = vec4(1.0, 1.0, 1.0, 1.0);
float diffuseIntensity = 0.2;
vec3 lightDir = vec3(0.2, 0.5, -1.0);

void main()
{
    vec4 ambient = ambientColor * ambientIntensity;
    vec4 diffuse = max(dot(fNormal, lightDir), 0.0) * diffuseColor * diffuseIntensity;

    fragColor = color * (diffuse + ambient);
    fragColor.a = 1.0;
}
