package com.yallo.sayle.core;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class SayleClient {
    private float viewportHalfWidth, viewportHalfHeight, aspectRatio;
    private int raysX, raysY;

    private RaycastInfo[][] depthField;
    private SayleServer server;

    public SayleClient(int terrainSampleResolution, float fovX, float fovY, SayleServer server) {
        viewportHalfWidth = (float) Math.tan(fovX / 2);
        viewportHalfHeight = (float) Math.tan(fovY / 2);
        aspectRatio = viewportHalfWidth / viewportHalfHeight;
        raysX = (int) (aspectRatio * terrainSampleResolution);
        raysY = terrainSampleResolution;

        depthField = new RaycastInfo[raysY][raysX];

        this.server = server;
    }

    private RaycastInfo[][] getDepthField(CharacterState state, RaycastableTerrain terrain) {
        for (int y = 0; y < raysY; y++) {
            for (int x = 0; x < raysX; x++) {
                Vector2f rayTilt = new Vector2f(2 * ((x + 0.5f) / raysX - 0.5f), -2 * ((y + 0.5f) / raysY - 0.5f));
                Vector3f dir = new Vector3f(state.right).mul(-rayTilt.x * viewportHalfWidth)
                        .add(new Vector3f(state.up).mul(rayTilt.y * viewportHalfHeight))
                        .add(new Vector3f(state.forward))
                        .normalize();

                RaycastInfo hit = terrain.raycast(new Vector3f(state.position), dir);

                if (!Float.isInfinite(hit.distance)) {
                    double deviation = dir.dot(state.forward);
                    hit.distance = (float) (hit.distance * Math.abs(deviation));
                }

                depthField[y][x] = hit;
            }
        }

        return depthField;
    }

    public Vector2f desiredInput(CharacterState state, RaycastableTerrain terrain) {
        return server.desiredInput(getDepthField(state, terrain));
    }
}
