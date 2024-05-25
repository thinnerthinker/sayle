package com.yallo.sayle.sandbox.game;

import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private static long handle;
    private static int width, height;

    public static void init(int width, int height, String title, long shared) {
        Window.width = width;
        Window.height = height;

        handle = glfwCreateWindow(width, height, title, NULL, shared);
        if (handle == NULL)
            throw new RuntimeException("Failed to create the GLFW Window");
    }

    public static long getHandle() {
        return handle;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }
}
