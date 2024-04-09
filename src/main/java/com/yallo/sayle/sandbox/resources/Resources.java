package com.yallo.sayle.sandbox.resources;

public class Resources {
    private static Shader terrainShader,  characterShader;

    public static void init() {
        terrainShader = loadShader("src/main/assets/model.vs", "src/main/assets/terrain.fs");
        characterShader = loadShader("src/main/assets/model.vs", "src/main/assets/player.fs");
    }

    private static Shader loadShader(String vertex, String fragment) {
        try {
            return new Shader(vertex, fragment);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public static Shader getTerrainShader() {
        return terrainShader;
    }
    public static Shader getCharacterShader() {
        return characterShader;
    }
}
