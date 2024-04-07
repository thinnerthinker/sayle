package com.yallo.sayle.core;

import org.joml.Vector2f;
import org.joml.Vector2i;

class TerrainSampleRegion {
    public Vector2i position;
    public int width, height;
    public RaycastInfo info;

    private Vector2f center;

    public TerrainSampleRegion(Vector2i position, int width, int height, RaycastInfo info) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.info = info;

        this.center = new Vector2f((position.x + width / 2f), (position.y + height / 2f));
    }

    public Vector2f getCenter() {
        return center;
    }
}
