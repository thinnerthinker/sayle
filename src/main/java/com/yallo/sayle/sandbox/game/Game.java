package com.yallo.sayle.sandbox.game;

import com.yallo.sayle.sandbox.input.Input;
import com.yallo.sayle.sandbox.resources.Resources;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public abstract class Game {
    // The flightWindow handle
    protected long flightWindow, depthFieldWindow;

    public void run() {
        init();
        loop();

        cleanup();

        // Free the flightWindow callbacks and destroy the flightWindow
        glfwFreeCallbacks(flightWindow);
        glfwDestroyWindow(flightWindow);

        glfwFreeCallbacks(depthFieldWindow);
        glfwDestroyWindow(depthFieldWindow);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    public abstract void initialize();
    public abstract void update(double dt);
    public abstract void draw();
    public abstract void drawDepthField();
    public abstract void dispose();

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        flightWindow = createWindow("Sayle Sandbox - Flight", NULL);
        depthFieldWindow = createWindow("Sayle Sandbox - Depth Field", flightWindow);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        glfwMakeContextCurrent(flightWindow);
        glfwSwapInterval(1);
        GL.createCapabilities();

        // Make the flightWindow visible
        glfwShowWindow(flightWindow);
        glfwShowWindow(depthFieldWindow);

        glfwSetInputMode(flightWindow, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Input.init(flightWindow);
        Resources.init();
        initialize();
    }

    private long createWindow(String title, long shared) {
        // Configure GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        // Create the flightWindow
        Window.init(1280, 720, title, shared);
        long windowHandle = Window.getHandle();

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the flightWindow size passed to glfwCreateWindow
            glfwGetWindowSize(windowHandle, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the flightWindow
            glfwSetWindowPos(
                    windowHandle,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        return windowHandle;
    }

    private void loop() {
        double currentTime;
        double prevTime = glfwGetTime();

        // Run the rendering loop until the user has attempted to close
        // the flightWindow or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(flightWindow) ) {
            // Variable timesteps (vsync still limits it though)
            currentTime = glfwGetTime();
            double elapsed = currentTime - prevTime;
            prevTime = currentTime;

            Input.update();
            update(elapsed);

            glfwMakeContextCurrent(flightWindow);
            glClearColor(100.0f / 255, 149.0f / 255, 237.0f / 255, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            draw();
            glfwSwapBuffers(flightWindow); // swap the color buffers

            glfwMakeContextCurrent(depthFieldWindow);
            glClearColor(0f / 255, 0f / 255, 0f / 255, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            drawDepthField();
            glfwSwapBuffers(depthFieldWindow); // swap the color buffers

            glfwMakeContextCurrent(flightWindow);

            // Poll for flightWindow events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void cleanup() {
        dispose();
    }
}
