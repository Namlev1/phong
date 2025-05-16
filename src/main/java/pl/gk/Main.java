package pl.gk;

import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    // Window handles
    private long window;

    // Window size
    private final int WIDTH = 800;
    private final int HEIGHT = 800;

    // Shader program
    private int shaderProgram;

    // VAO and VBO handles
    private int vao;
    private int vbo;
    private int ebo;

    // Material ID
    private int materialId = 0;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        System.out.println("LWJGL Version: " + Version.getVersion());

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    private void init() {
        // Setup an error callback
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Phong Lighting Model", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            }

            // Change material with number keys
            if (action == GLFW_PRESS) {
                if (key == GLFW_KEY_1) {
                    materialId = 0; // Metal
                    System.out.println("Material: Metal (kierunkowe odbicie)");
                } else if (key == GLFW_KEY_2) {
                    materialId = 1; // Wall
                    System.out.println("Material: Wall (odbicie rozproszone)");
                } else if (key == GLFW_KEY_3) {
                    materialId = 2; // Wood
                    System.out.println("Material: Wood (pomiędzy)");
                } else if (key == GLFW_KEY_4) {
                    materialId = 3; // Plastic
                    System.out.println("Material: Plastic (pomiędzy)");
                }
            }
        });

        // Center the window on the screen
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            if (vidmode != null) {
                glfwSetWindowPos(
                        window,
                        (vidmode.width() - pWidth.get(0)) / 2,
                        (vidmode.height() - pHeight.get(0)) / 2
                );
            }
        }

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        // Enable depth testing
        glEnable(GL_DEPTH_TEST);

        // Setup sphere
        setupSphere();

        // Setup shaders
        setupShaders();
    }

    private void setupSphere() {
        // Create and bind VAO
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        // Create sphere vertices, normals, and indices using SphereGenerator
        SphereGenerator sphere = new SphereGenerator(1.0f, 32, 32);
        float[] vertices = sphere.getVertices();
        int[] indices = sphere.getIndices();

        // Create and bind VBO for vertices
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        // Create and bind EBO for indices
        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // Position attribute
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // Normal attribute
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Unbind VAO
        glBindVertexArray(0);
    }

    private void setupShaders() {
        // Vertex shader
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, ShaderSource.VERTEX_SHADER);
        glCompileShader(vertexShader);
        checkShaderCompileStatus(vertexShader, "Vertex");

        // Fragment shader
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, ShaderSource.FRAGMENT_SHADER);
        glCompileShader(fragmentShader);
        checkShaderCompileStatus(fragmentShader, "Fragment");

        // Link shaders
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        checkProgramLinkStatus(shaderProgram);

        // Delete shaders as they're linked into our program now and no longer necessary
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private void checkShaderCompileStatus(int shader, String type) {
        int status = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (status == GL_FALSE) {
            String log = glGetShaderInfoLog(shader);
            System.err.println(type + " shader compilation failed: " + log);
            throw new RuntimeException(type + " shader compilation failed");
        }
    }

    private void checkProgramLinkStatus(int program) {
        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == GL_FALSE) {
            String log = glGetProgramInfoLog(program);
            System.err.println("Program linking failed: " + log);
            throw new RuntimeException("Program linking failed");
        }
    }

    private void loop() {
        // Set up rotation variables
        float angle = 0.0f;

        // Set up timing variables
        float lastTime = (float) glfwGetTime();

        // Rendering loop
        while (!glfwWindowShouldClose(window)) {
            // Calculate timing
            float currentTime = (float) glfwGetTime();
            float deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            // Update rotation angle
            angle += 30.0f * deltaTime; // 30 degrees per second

            // Clear the framebuffer
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Activate shader
            glUseProgram(shaderProgram);

            // Update view position
            float viewX = (float) Math.sin(Math.toRadians(angle)) * 3.0f;
            float viewZ = (float) Math.cos(Math.toRadians(angle)) * 3.0f;
            glUniform3f(glGetUniformLocation(shaderProgram, "viewPos"), viewX, 1.5f, viewZ);

            // Create view matrix (camera)
            float[] viewMatrix = Mat4.lookAt(
                    viewX, 1.5f, viewZ,  // camera position
                    0.0f, 0.0f, 0.0f,     // camera target
                    0.0f, 1.0f, 0.0f      // up vector
            );
            int viewLoc = glGetUniformLocation(shaderProgram, "view");
            glUniformMatrix4fv(viewLoc, false, viewMatrix);

            // Create projection matrix
            float[] projMatrix = Mat4.perspective(45.0f, (float) WIDTH / HEIGHT, 0.1f, 100.0f);
            int projLoc = glGetUniformLocation(shaderProgram, "projection");
            glUniformMatrix4fv(projLoc, false, projMatrix);

            // Create model matrix
            float[] modelMatrix = Mat4.identity();
            int modelLoc = glGetUniformLocation(shaderProgram, "model");
            glUniformMatrix4fv(modelLoc, false, modelMatrix);

            // Set light properties
            glUniform3f(glGetUniformLocation(shaderProgram, "lightPos"), 2.0f, 2.0f, 2.0f);
            glUniform3f(glGetUniformLocation(shaderProgram, "lightColor"), 1.0f, 1.0f, 1.0f);

            // Set material properties based on materialId
            setMaterial(materialId);

            // Draw the sphere
            glBindVertexArray(vao);
            SphereGenerator sphere = new SphereGenerator(1.0f, 32, 32);
            glDrawElements(GL_TRIANGLES, sphere.getIndexCount(), GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);

            // Swap the color buffers
            glfwSwapBuffers(window);

            // Poll for window events
            glfwPollEvents();
        }
    }

    private void setMaterial(int id) {
        int materialLoc = glGetUniformLocation(shaderProgram, "materialId");
        glUniform1i(materialLoc, id);

        switch (id) {
            case 0: // Metal (kierunkowe odbicie)
                glUniform3f(glGetUniformLocation(shaderProgram, "material.ambient"), 0.25f, 0.25f, 0.25f);
                glUniform3f(glGetUniformLocation(shaderProgram, "material.diffuse"), 0.4f, 0.4f, 0.4f);
                glUniform3f(glGetUniformLocation(shaderProgram, "material.specular"), 0.774597f, 0.774597f, 0.774597f);
                glUniform1f(glGetUniformLocation(shaderProgram, "material.shininess"), 76.8f);
                break;
            case 1: // Wall (odbicie rozproszone)
                glUniform3f(glGetUniformLocation(shaderProgram, "material.ambient"), 0.05f, 0.05f, 0.05f);
                glUniform3f(glGetUniformLocation(shaderProgram, "material.diffuse"), 0.55f, 0.55f, 0.55f);
                glUniform3f(glGetUniformLocation(shaderProgram, "material.specular"), 0.07f, 0.07f, 0.07f);
                glUniform1f(glGetUniformLocation(shaderProgram, "material.shininess"), 2.8f);
                break;
            case 2: // Wood (pomiędzy)
                glUniform3f(glGetUniformLocation(shaderProgram, "material.ambient"), 0.1f, 0.05f, 0.0f);
                glUniform3f(glGetUniformLocation(shaderProgram, "material.diffuse"), 0.5f, 0.25f, 0.0f);
                glUniform3f(glGetUniformLocation(shaderProgram, "material.specular"), 0.3f, 0.15f, 0.0f);
                glUniform1f(glGetUniformLocation(shaderProgram, "material.shininess"), 32.0f);
                break;
            case 3: // Plastic (pomiędzy)
                glUniform3f(glGetUniformLocation(shaderProgram, "material.ambient"), 0.0f, 0.1f, 0.06f);
                glUniform3f(glGetUniformLocation(shaderProgram, "material.diffuse"), 0.0f, 0.51f, 0.3f);
                glUniform3f(glGetUniformLocation(shaderProgram, "material.specular"), 0.5f, 0.5f, 0.5f);
                glUniform1f(glGetUniformLocation(shaderProgram, "material.shininess"), 32.0f);
                break;
        }
    }
}