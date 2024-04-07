package com.yallo.sayle.sandbox.resources;

public class Resources {
    private static Shader terrainShader;

    public static void init() {
        terrainShader = loadShader("src/main/Assets/terrain.vs", "src/main/Assets/terrain.fs");
    }

    private static Shader loadShader(String vertex, String fragment) {
        try {
            return new Shader(vertex, fragment);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Shader getTerrainShader() {
        return terrainShader;
    }
}
