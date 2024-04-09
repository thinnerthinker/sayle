#version 330

uniform vec3 cameraPos;  // Camera position in world space
float fadeStart = 80.0; // Distance at which fading starts
float fadeEnd = 120.0;   // Distance at which objects are fully faded

in vec3 fPos;     // Fragment position in world space
in vec3 fNormal;  // Normal at the fragment

out vec4 fragColor;

vec4 color = vec4(255.0 / 255, 145.0 / 255, 0.0 / 255, 1.0);

vec4 ambientColor = vec4(1.0, 1.0, 1.0, 1.0);
float ambientIntensity = 0.8;

vec4 diffuseColor = vec4(1.0, 1.0, 1.0, 1.0);
float diffuseIntensity = 0.5;
vec3 lightDir = normalize(vec3(0.2, 0.5, 1.0)); // Ensure the light direction is normalized

void main()
{
    vec4 ambient = ambientColor * ambientIntensity;
    vec3 norm = normalize(fNormal);
    vec4 diffuse = max(dot(norm, lightDir), 0.0) * diffuseColor * diffuseIntensity;

    float distance = length(cameraPos - fPos);

    float fadeFactor = clamp((distance - fadeStart) / (fadeEnd - fadeStart), 0.0, 1.0);

    vec4 backgroundColor = vec4(100.0 / 255.0, 149.0 / 255.0, 237.0 / 255.0, 1.0);
    vec4 finalColor = mix(color * (diffuse + ambient), backgroundColor, fadeFactor);

    fragColor = vec4(finalColor.rgb, color.a);
}
