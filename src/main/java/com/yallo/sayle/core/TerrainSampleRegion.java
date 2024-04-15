package com.yallo.sayle.core;

import org.joml.Vector2f;
import org.joml.Vector2i;

public class TerrainSampleRegion {
    public Vector2i position;
    public int width, height;
    public float distance;
    public RaycastInfo original;

    private Vector2f center;

    public TerrainSampleRegion(Vector2i position, int width, int height, float distance, RaycastInfo original) {
        this.position = position;
        this.width = width;
        this.height = height;
        this.distance = distance;
        this.original = original;

        this.center = new Vector2f((position.x + width / 2f), (position.y + height / 2f));
    }

    public Vector2f getCenter() {
        return center;
    }

    public Vector2f closestPointTowards(Vector2f target) {
        if (target.x >= position.x && target.x <= (position.x + width) &&
                target.y >= position.y && target.y <= (position.y + height)) {
            return target;
        }

        Vector2f dir = new Vector2f(target.x - center.x, target.y - center.y).normalize();

        float halfWidth = width / 2f;
        float halfHeight = height / 2f;

        float xProjection = halfWidth * dir.x;
        float yProjection = halfHeight * dir.y;

        xProjection = Math.max(-halfWidth, Math.min(xProjection, halfWidth));
        yProjection = Math.max(-halfHeight, Math.min(yProjection, halfHeight));

        return new Vector2f(center.x + xProjection, center.y + yProjection);
    }

}
