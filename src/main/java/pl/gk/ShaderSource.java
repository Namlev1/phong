package pl.gk;

public class ShaderSource {
    public static final String VERTEX_SHADER =
            "#version 330 core\n" +
                    "layout (location = 0) in vec3 aPos;\n" +
                    "layout (location = 1) in vec3 aNormal;\n" +
                    "\n" +
                    "out vec3 FragPos;\n" +
                    "out vec3 Normal;\n" +
                    "\n" +
                    "uniform mat4 model;\n" +
                    "uniform mat4 view;\n" +
                    "uniform mat4 projection;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    FragPos = vec3(model * vec4(aPos, 1.0));\n" +
                    "    Normal = mat3(transpose(inverse(model))) * aNormal;\n" +
                    "    \n" +
                    "    gl_Position = projection * view * vec4(FragPos, 1.0);\n" +
                    "}";

    public static final String FRAGMENT_SHADER =
            "#version 330 core\n" +
                    "out vec4 FragColor;\n" +
                    "\n" +
                    "in vec3 FragPos;\n" +
                    "in vec3 Normal;\n" +
                    "\n" +
                    "struct Material {\n" +
                    "    vec3 ambient;\n" +
                    "    vec3 diffuse;\n" +
                    "    vec3 specular;\n" +
                    "    float shininess;\n" +
                    "};\n" +
                    "\n" +
                    "uniform vec3 lightPos;\n" +
                    "uniform vec3 viewPos;\n" +
                    "uniform vec3 lightColor;\n" +
                    "uniform Material material;\n" +
                    "uniform int materialId;\n" +
                    "\n" +
                    "void main()\n" +
                    "{\n" +
                    "    // Normalize vectors\n" +
                    "    vec3 norm = normalize(Normal);\n" +
                    "    vec3 lightDir = normalize(lightPos - FragPos);\n" +
                    "    \n" +
                    "    // Ambient component\n" +
                    "    vec3 ambient = material.ambient * lightColor;\n" +
                    "    \n" +
                    "    // Diffuse component\n" +
                    "    float diff = max(dot(norm, lightDir), 0.0);\n" +
                    "    vec3 diffuse = diff * material.diffuse * lightColor;\n" +
                    "    \n" +
                    "    // Specular component (Phong reflection model)\n" +
                    "    vec3 viewDir = normalize(viewPos - FragPos);\n" +
                    "    vec3 reflectDir = reflect(-lightDir, norm);\n" +
                    "    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);\n" +
                    "    vec3 specular = spec * material.specular * lightColor;\n" +
                    "    \n" +
                    "    // Apply different materials\n" +
                    "    vec3 baseColor;\n" +
                    "    if (materialId == 0) { // Metal\n" +
                    "        baseColor = vec3(0.8, 0.8, 0.8); // Silver-like color\n" +
                    "    } else if (materialId == 1) { // Wall\n" +
                    "        baseColor = vec3(0.9, 0.85, 0.7); // Off-white color\n" +
                    "    } else if (materialId == 2) { // Wood\n" +
                    "        baseColor = vec3(0.6, 0.3, 0.1); // Brown color\n" +
                    "    } else { // Plastic\n" +
                    "        baseColor = vec3(0.2, 0.7, 0.2); // Green color\n" +
                    "    }\n" +
                    "    \n" +
                    "    // Combine all components\n" +
                    "    vec3 result = (ambient + diffuse + specular) * baseColor;\n" +
                    "    \n" +
                    "    FragColor = vec4(result, 1.0);\n" +
                    "}";
}
