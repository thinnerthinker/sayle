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
    private float viewportHalfWidth, viewportHalfHeight, aspectRatio;
    private int raysX, raysY;

    RaycastInfo[][] depthField, quantizedDepthField;

    public FlightBehavior(int terrainSampleResolution, float maxDepthDelta, float fovX, float fovY) {
        this.terrainSampleResolution = terrainSampleResolution;
        this.maxDepthDelta = maxDepthDelta;
        this.fovX = fovX;
        this.fovY = fovY;
        viewportHalfWidth = (float) Math.tan(fovX / 2);
        viewportHalfHeight = (float) Math.tan(fovY / 2);
        aspectRatio = viewportHalfWidth / viewportHalfHeight;
        raysX = (int) (aspectRatio * terrainSampleResolution);
        raysY = terrainSampleResolution;

        depthField = new RaycastInfo[raysY][raysX];
        quantizedDepthField = new RaycastInfo[raysY][raysX];
    }

    public RaycastInfo[][] getDepthField(CharacterState state, RaycastableTerrain terrain) {
        for (int y = 0; y < raysY; y++) {
            for (int x = 0; x < raysX; x++) {
                Vector2f rayTilt = new Vector2f(2 * ((x + 0.5f) / raysX - 0.5f), -2 * ((y + 0.5f) / raysY - 0.5f));
                Vector3f dir = new Vector3f(state.right).mul(rayTilt.x * viewportHalfWidth)
                        .add(new Vector3f(state.up).mul(-rayTilt.y * viewportHalfHeight))
                        .add(new Vector3f(state.forward))
                        .normalize();

                RaycastInfo hit = terrain.raycast(new Vector3f(state.position), dir);

                if (!Float.isInfinite(hit.distance)) {
                    double deviation = Math.acos(dir.dot(state.forward));
                    //if (Math.abs(deviation) < Math.PI / 4) {
                    hit.distance = (float) (hit.distance * Math.abs(Math.cos(deviation)));
                    //}
                }

                depthField[y][x] = hit;
            }
        }

        return depthField;
    }

    public RaycastInfo[][] quantizedDepthField(RaycastInfo[][] depthField) {
        return coverDangerousEdges(depthField, 2 * viewportHalfWidth, 2 * viewportHalfHeight);
    }

    public Vector2f desiredInput(RaycastInfo[][] depthField, RegionEvaluatorFunction eval) {
        float viewportHalfWidth = (float) Math.tan(fovX / 2), viewportHalfHeight = (float) Math.tan(fovY / 2);
        float aspectRatio = viewportHalfWidth / viewportHalfHeight;
        int raysX = (int) (aspectRatio * terrainSampleResolution), raysY = terrainSampleResolution;

        var bestEval = getRegions(depthField).stream().map(eval::calculate).min(Comparator.comparing(e -> e.cost)).get();

        return bestEval.suggestedDirection;
    }

    RaycastInfo[][] coverDangerousEdges(RaycastInfo[][] sample, float viewportWidth, float viewportHeight) {
        final float safeDistance = 1f, distTolerance = 0.01f;

        int raysY = sample.length, raysX = sample[0].length;
        RaycastInfo[][] covered = this.quantizedDepthField;
        for (int y = 0; y < raysY; y++) {
            for (int x = 0; x < raysX; x++) {
                covered[y][x] = new RaycastInfo(sample[y][x]);
            }
        }

        for (int y = 0; y < raysY; y++) {
            int coverDistance = 0;
            float coverAmount = Float.POSITIVE_INFINITY;

            for (int x = 0; x < raysX; x++) {
                if (distanceDelta(sample[y][x].distance, coverAmount, distTolerance) <= 0) {
                    coverAmount = sample[y][x].distance;
                    coverDistance = (int) Math.ceil(raysX * safeDistance / coverAmount * viewportWidth);
                }

                if (coverDistance > 0) {
                    covered[y][x].distance = Math.min(covered[y][x].distance, coverAmount);
                    coverDistance--;

                    if (coverDistance == 0) {
                        coverAmount = Float.POSITIVE_INFINITY;
                    }
                }
            }
        }

        for (int y = 0; y < raysY; y++) {
            int coverDistance = 0;
            float coverAmount = Float.POSITIVE_INFINITY;

            for (int x = raysX - 1; x > 0; x--) {
                if (distanceDelta(sample[y][x].distance, coverAmount, distTolerance) <= 0) {
                    coverAmount = sample[y][x].distance;
                    coverDistance = (int) Math.ceil(raysX * safeDistance / coverAmount * viewportWidth);
                }

                if (coverDistance > 0) {
                    covered[y][x].distance = Math.min(covered[y][x].distance, coverAmount);
                    coverDistance--;

                    if (coverDistance == 0) {
                        coverAmount = Float.POSITIVE_INFINITY;
                    }
                }
            }
        }

        for (int x = 0; x < raysX; x++) {
            int coverDistance = 0;
            float coverAmount = Float.POSITIVE_INFINITY;

            for (int y = 0; y < raysY; y++) {
                if (distanceDelta(sample[y][x].distance, coverAmount, distTolerance) <= 0) {
                    coverAmount = sample[y][x].distance;
                    coverDistance = (int) Math.ceil(raysX * safeDistance / coverAmount * viewportWidth);
                }

                if (coverDistance > 0) {
                    covered[y][x].distance = Math.min(covered[y][x].distance, coverAmount);
                    coverDistance--;

                    if (coverDistance == 0) {
                        coverAmount = Float.POSITIVE_INFINITY;
                    }
                }
            }
        }

        for (int x = 0; x < raysX; x++) {
            int coverDistance = 0;
            float coverAmount = Float.POSITIVE_INFINITY;

            for (int y = raysY - 1; y > 0; y--) {
                if (distanceDelta(sample[y][x].distance, coverAmount, distTolerance) <= 0) {
                    coverAmount = sample[y][x].distance;
                    coverDistance = (int) Math.ceil(raysX * safeDistance / coverAmount * viewportWidth);
                }

                if (coverDistance > 0) {
                    covered[y][x].distance = Math.min(covered[y][x].distance, coverAmount);
                    coverDistance--;

                    if (coverDistance == 0) {
                        coverAmount = Float.POSITIVE_INFINITY;
                    }
                }
            }
        }

        return covered;
    }

    float distanceDelta(float current, float previous, float tolerance) {
        float d = current - previous;
        if (Math.abs(d) <= tolerance) {
            return 0;
        }
        return d;
    }

    List<TerrainSampleRegion> getRegions(RaycastInfo[][] sample) {
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
                    if (visited.contains(new Vector2i(x + width, y)) || !current.similar(sample[y][x + width], maxDepthDelta)) {
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
