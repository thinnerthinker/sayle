package com.yallo.sayle.core;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class FlightBehavior {
    private int terrainSampleResolution;
    private float maxDepthDelta;

    private float fovX, fovY;

    public FlightBehavior(int terrainSampleResolution, float maxDepthDelta, float fovX, float fovY) {
        this.terrainSampleResolution = terrainSampleResolution;
        this.maxDepthDelta = maxDepthDelta;
        this.fovX = fovX;
        this.fovY = fovY;
    }

    public Vector2f desiredInput(CharacterState state, RaycastableTerrain terrain, RegionEvaluatorFunction eval) {
        float viewportHalfWidth = (float) Math.tan(fovX / 2), viewportHalfHeight = (float) Math.tan(fovY / 2);
        float aspectRatio = viewportHalfWidth / viewportHalfHeight;

        int raysX = (int) (aspectRatio * terrainSampleResolution), raysY = terrainSampleResolution;
        RaycastInfo[][] depthField = new RaycastInfo[raysY][raysX];


        for (int y = 0; y < raysY; y++) {
            for (int x = 0; x < raysX; x++) {
                Vector2f rayTilt = new Vector2f(2 * ((x + 0.5f) / raysX - 0.5f), 2 * ((y + 0.5f) / raysY - 0.5f));
                Vector3f dir = new Vector3f(state.right).mul(rayTilt.x * viewportHalfWidth)
                        .add(new Vector3f(state.up).mul(-rayTilt.y * viewportHalfHeight))
                        .add(new Vector3f(state.forward))
                        .normalize();

                RaycastInfo hit = terrain.raycast(new Vector3f(state.position), dir);

                if (!Float.isInfinite(hit.distance)) {
                    double wallAngle = Math.acos(dir.dot(hit.normal));
                    hit.distance = (float) (hit.distance * Math.cos(wallAngle));
                }

                depthField[y][x] = hit;
            }
        }

        /*for (int y = 0; y < raysY; y++) {
            for (int x = 0; x < raysX; x++) {
                System.out.print(String.format("%.2f ", depthField[y][x].distance));
            }
            System.out.println();
        }
        System.out.println();*/

        var bestEval = getRectRegions(depthField).stream().map(eval::calculate).min(Comparator.comparing(e -> e.cost)).get();
        //System.out.println(bestHole.position.x + " " + bestHole.position.y + " " + bestHole.width + " " + bestHole.height);

        Vector2f target = bestEval.suggestedPoint;
        return new Vector2f(2 * (target.x / raysX - 0.5f), 2 * (target.y / raysY - 0.5f));
    }

    List<TerrainSampleRegion> getRectRegions(RaycastInfo[][] sample) {
        List<TerrainSampleRegion> regions = new ArrayList<>();
        HashSet<Vector2i> visited = new HashSet<>();

        for (int y = 0; y < sample.length; y++) {
            for (int x = 0; x < sample[0].length; x++) {
                if (visited.contains(new Vector2i(x, y))) {
                    continue;
                }

                RaycastInfo current = sample[y][x];
                int maxWidth = sample[0].length - x;
                int maxHeight = sample.length - y;

                int width;
                for (width = 1; width < maxWidth; width++) {
                    if (!current.similar(sample[y][x + width], maxDepthDelta)) {
                        break;
                    }
                }

                int height;
                outerLoop:
                for (height = 1; height < maxHeight; height++) {
                    for (int wx = 0; wx < width; wx++) {
                        if (!current.similar(sample[y + height][x + wx], maxDepthDelta)) {
                            break outerLoop;
                        }
                    }
                }

                // Mark all cells in the region as visited
                for (int wy = 0; wy < height; wy++) {
                    for (int wx = 0; wx < width; wx++) {
                        visited.add(new Vector2i(x + wx, y + wy));
                    }
                }

                regions.add(new TerrainSampleRegion(new Vector2i(x, y), width, height, current));
            }
        }

        return regions;
    }

}
