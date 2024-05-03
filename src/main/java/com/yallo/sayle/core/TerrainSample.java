package com.yallo.sayle.core;

import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class TerrainSample {
    public int width, height;
    public RaycastInfo[][] raw;
    public float[][] quantized, covered, pathScores;
    public float viewportWidth, viewportHeight;

    public List<List<Vector2i>> paths;

    public TerrainSample(RaycastInfo[][] raw, float viewportWidth, float viewportHeight) {
        this.raw = raw;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;

        this.width = raw[0].length;
        this.height = raw.length;

        quantized = new float[height][width];
        covered = new float[height][width];

        pathScores = new float[height][width];

        paths = new ArrayList<>();

        calculate();
    }

    private void calculate() {
        quantized = quantize(raw);
        covered = coverDangerousEdges(quantized);
        pathScores = getPathScores(raw, covered);
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

    float[][] getPathScores(RaycastInfo[][] sample, float[][] distances) {
        int centerX = width / 2;
        int centerY = height / 2;

        paths.clear();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int ix = x;
                int iy = y;

                int dx = Math.abs(centerX - ix);
                int dy = Math.abs(centerY - iy);
                int sx = ix < centerX ? 1 : -1;
                int sy = iy < centerY ? 1 : -1;
                int err = (dx > dy ? dx : -dy) / 2;
                int e2;

                double score = 0.0;
                List<Vector2i> path = new ArrayList<>();

                //System.out.printf("start from (%d, %d)\n", ix, iy);

                while (true) {
                    // Process the current cell
                    float distance = clampDistance(distances[iy][ix]);
                    path.add(new Vector2i(ix, iy));
                    score += distance;

                    //System.out.printf("(%d, %d) - Score: %.2f\n", ix, iy, distance);

                    if (ix == centerX && iy == centerY) {
                        break;
                    }

                    e2 = err;
                    if (e2 > -dx) {
                        err -= dy;
                        ix += sx;
                    }
                    if (e2 < dy) {
                        err += dx;
                        iy += sy;
                    }
                }

                var firstCell = path.get(0);
                float dist = distances[firstCell.y][firstCell.x];
                int len = 1;
                for (int i = 1; i < path.size(); i++) {
                    var cell = path.get(i);

                    if (Math.abs(distances[cell.y][cell.x] - dist) > 1f) {
                        break;
                    }
                    len++;
                }

                pathScores[y][x] = (float) (dist / Math.pow(len, 0.7f));
                paths.add(path);  // Add the path for this start point

                //System.out.println("end" + path.size());
            }
        }

        return pathScores;
    }

    private float clampDistance(double distance) {
        return (float) (1 - Math.exp(-distance));
    }
}
