package com.yallo.sayle.core;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class TerrainSample {
    public int width, height;
    public RaycastInfo[][] raw;
    public float[][] quantized, covered;
    public List<TerrainSampleRegion> regions;
    public float viewportWidth, viewportHeight;

    private boolean[][] visited;

    public TerrainSample(RaycastInfo[][] raw, float viewportWidth, float viewportHeight) {
        this.raw = raw;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;

        this.width = raw[0].length;
        this.height = raw.length;

        quantized = new float[height][width];
        covered = new float[height][width];

        visited = new boolean[height][width];
        regions = new ArrayList<>();

        calculate();
    }

    private void calculate() {
        quantized = quantize(raw);
        covered = coverDangerousEdges(quantized);
        regions = extractRegions(raw, covered);
    }

    private float[][] quantize(RaycastInfo[][] sample) {
        int buckets = Parameters.quantizationBuckets;
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;

        float m = 100, k = 100;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float dist = sample[y][x].distance;
                if (Float.isInfinite(dist)) {
                    dist = m;
                }

                float mapped = (m * dist) / (dist + k);
                quantized[y][x] = dist;
                quantized[y][x] = mapped;

                if (mapped < min) min = mapped;
                if (mapped > max) max = mapped;
            }
        }

        float range = max - min;
        float interval = range / buckets;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float normalized = (float) Math.pow((sample[y][x].distance - min) / range, 2f);
                int bucketIndex = Math.min((int) (normalized * buckets), buckets - 1);
                float quant = min + bucketIndex * interval;

                quantized[y][x] = -quant * k / (quant - m);
            }
        }

        return quantized;
    }


    private float distanceDelta(float current, float previous, float tolerance) {
        float d = current - previous;
        if (Math.abs(d) <= tolerance) {
            return 0;
        }
        return d;
    }

    float[][] coverDangerousEdges(float[][] distances) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                covered[y][x] = distances[y][x];
            }
        }

        for (int y = 0; y < height; y++) {
            int coverDistance = 0;
            float coverAmount = Float.POSITIVE_INFINITY;

            for (int x = 0; x < width; x++) {
                if (distanceDelta(distances[y][x], coverAmount, Parameters.coveringDistanceTolerance) <= 0) {
                    coverAmount = distances[y][x];
                    coverDistance = (int) Math.ceil(width * Parameters.coveringSafeDistance / coverAmount * viewportWidth);
                }

                if (coverDistance > 0) {
                    covered[y][x] = Math.min(covered[y][x], coverAmount);
                    coverDistance--;

                    if (coverDistance == 0) {
                        coverAmount = Float.POSITIVE_INFINITY;
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            int coverDistance = 0;
            float coverAmount = Float.POSITIVE_INFINITY;

            for (int x = width - 1; x > 0; x--) {
                if (distanceDelta(distances[y][x], coverAmount, Parameters.coveringDistanceTolerance) <= 0) {
                    coverAmount = distances[y][x];
                    coverDistance = (int) Math.ceil(width * Parameters.coveringSafeDistance / coverAmount * viewportWidth);
                }

                if (coverDistance > 0) {
                    covered[y][x] = Math.min(covered[y][x], coverAmount);
                    coverDistance--;

                    if (coverDistance == 0) {
                        coverAmount = Float.POSITIVE_INFINITY;
                    }
                }
            }
        }

        for (int x = 0; x < width; x++) {
            int coverDistance = 0;
            float coverAmount = Float.POSITIVE_INFINITY;

            for (int y = 0; y < width; y++) {
                if (distanceDelta(distances[y][x], coverAmount, Parameters.coveringDistanceTolerance) <= 0) {
                    coverAmount = distances[y][x];
                    coverDistance = (int) Math.ceil(height * Parameters.coveringSafeDistance / coverAmount * viewportHeight);
                }

                if (coverDistance > 0) {
                    covered[y][x] = Math.min(covered[y][x], coverAmount);
                    coverDistance--;

                    if (coverDistance == 0) {
                        coverAmount = Float.POSITIVE_INFINITY;
                    }
                }
            }
        }

        for (int x = 0; x < width; x++) {
            int coverDistance = 0;
            float coverAmount = Float.POSITIVE_INFINITY;

            for (int y = height - 1; y > 0; y--) {
                if (distanceDelta(distances[y][x], coverAmount, Parameters.coveringDistanceTolerance) <= 0) {
                    coverAmount = distances[y][x];
                    coverDistance = (int) Math.ceil(height * Parameters.coveringSafeDistance / coverAmount * viewportHeight);
                }

                if (coverDistance > 0) {
                    covered[y][x] = Math.min(covered[y][x], coverAmount);
                    coverDistance--;

                    if (coverDistance == 0) {
                        coverAmount = Float.POSITIVE_INFINITY;
                    }
                }
            }
        }

        return covered;
    }

    List<TerrainSampleRegion> extractRegions(RaycastInfo[][] sample, float[][] distances) {
        regions.clear();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                visited[y][x] = false;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (visited[y][x]) {
                    continue;
                }

                int maxWidth = width - x;
                int maxHeight = height - y;

                int regWidth;
                for (regWidth = 1; regWidth < maxWidth; regWidth++) {
                    if (visited[y][x + regWidth] || !(sample[y][x].solid == sample[y][x + regWidth].solid &&
                            distances[y][x] == distances[y][x + regWidth])) {
                        break;
                    }
                }

                int regHeight;
                outerLoop:
                for (regHeight = 1; regHeight < maxHeight; regHeight++) {
                    for (int rx = 0; rx < regWidth; rx++) {
                        if (!(sample[y][x].solid == sample[y + regHeight][x + rx].solid &&
                                distances[y][x] == distances[y + regHeight][x + rx])) {
                            break outerLoop;
                        }
                    }
                }

                for (int ry = 0; ry < regHeight; ry++) {
                    for (int rx = 0; rx < regWidth; rx++) {
                        visited[y + ry][x + rx] = true;
                    }
                }

                regions.add(new TerrainSampleRegion(new Vector2i(x, y), regWidth, regHeight, distances[y][x], sample[y][x]));
            }
        }

        return regions;
    }

    public List<TerrainSampleRegion> getRegions() {
        return regions;
    }
}
