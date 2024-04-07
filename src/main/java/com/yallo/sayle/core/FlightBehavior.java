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

    public Vector2f desiredInput(CharacterState state, RaycastableTerrain terrain, RegionCostFunction cost) {
        float viewportHalfWidth = (float) Math.tan(fovX), viewportHalfHeight = (float) Math.tan(fovY);
        float aspectRatio = viewportHalfWidth / viewportHalfHeight;

        int raysX = (int) (aspectRatio * terrainSampleResolution), raysY = terrainSampleResolution;
        RaycastInfo[][] depthField = new RaycastInfo[raysX][raysY];

        for (int i = 0; i < raysX; i++) {
            for (int j = 0; j < raysY; j++) {
                Vector2f rayTilt = new Vector2f(2 * ((i + 0.5f) / raysX - 0.5f), 2 * ((j + 0.5f) / raysY - 0.5f));
                Vector3f dir = state.right.mul(rayTilt.x * viewportHalfWidth)
                        .add(state.up.mul(rayTilt.y * viewportHalfHeight))
                        .add(state.forward)
                        .normalize();

                depthField[i][j] = terrain.raycast(state.position, dir);
            }
        }

        TerrainSampleRegion bestHole = getRectRegions(depthField).stream().min(Comparator.comparing(cost::calculate)).get();
        Vector2f holeCenter = bestHole.getCenter();

        return new Vector2f(2 * (holeCenter.x / raysX - 0.5f), 2 * (holeCenter.y / raysY - 0.5f));
    }

    List<TerrainSampleRegion> getRectRegions(RaycastInfo[][] sample) {
        // TODO: currently using greedy meshing. maybe try backtracking an optimal solution?
        List<TerrainSampleRegion> regions = new ArrayList<>();

        HashSet<Vector2i> visited = new HashSet<>();

        for (int i = 0; i < sample.length; i++) {
            for (int j = 0; j < sample[0].length; j++) {
                if (visited.contains(new Vector2i(i, j))) {
                    continue;
                }

                RaycastInfo current = sample[i][j];

                int width = 0;
                for (; i + width < sample.length; width++) {
                    if (!current.similar(sample[i + width][j], maxDepthDelta)) {
                        break;
                    }

                    visited.add(new Vector2i(i + width, j));
                }

                int height = 1;
                for (; j + height < sample[0].length; height++) {
                    boolean interrupted = false;

                    for (int x = 0; x < width; x++) {
                        if (!current.similar(sample[i + width][j], maxDepthDelta)) {
                            interrupted = true;
                            break;
                        }
                    }

                    if (interrupted) {
                        break;
                    }

                    for (int x = 0; x < width; x++) {
                        visited.add(new Vector2i(i + width, j));
                    }
                }

                regions.add(new TerrainSampleRegion(new Vector2i(i , j), width, height, current));
            }
        }

        return regions;
    }

    List<TerrainSampleRegion> getRegions(RaycastInfo[][] sample) {
        List<TerrainSampleRegion> regions = new ArrayList<>();

        HashSet<Vector2i> visited = new HashSet<>();
        ArrayList<Vector2i> queue = new ArrayList<>();

        for (int i = 0; i < sample.length; i++) {
            for (int j = 0; j < sample[0].length; j++) {
                Vector2i startVertex = new Vector2i(i, j);
                if (visited.contains(startVertex)) {
                    continue;
                }

                RaycastInfo startInfo = sample[i][j];
                TerrainSampleRegion region = new TerrainSampleRegion(startVertex, 1, 1, startInfo);
                regions.add(region);

                queue.add(startVertex);

                for (int v = 0; v < queue.size(); v++) {
                    Vector2i vertex = queue.get(v);
                    if (visited.contains(vertex)) {
                        continue;
                    }

                    region.position.x = Math.min(region.position.x, vertex.x);
                    region.position.y = Math.min(region.position.y, vertex.y);
                    region.width = Math.max(region.width, vertex.x - region.position.x + 1);
                    region.height = Math.max(region.height, vertex.y - region.position.y + 1);

                    visited.add(queue.get(v));

                    if (i > 0 && sample[i - 1][j].similar(startInfo, maxDepthDelta))
                        queue.add(new Vector2i(i - 1, j));
                    if (i < sample.length - 1 && sample[i + 1][j].similar(startInfo, maxDepthDelta))
                        queue.add(new Vector2i(i + 1, j));
                    if (j > 0 && sample[i][j - 1].similar(startInfo, maxDepthDelta))
                        queue.add(new Vector2i(i, j - 1));
                    if (j < sample[0].length - 1 && sample[i][j + 1].similar(startInfo, maxDepthDelta))
                        queue.add(new Vector2i(i, j + 1));
                }

                queue.clear();
            }
        }

        return regions;
    }
}
