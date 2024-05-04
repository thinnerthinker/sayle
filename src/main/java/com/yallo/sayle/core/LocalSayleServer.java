package com.yallo.sayle.core;

import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LocalSayleServer implements SayleServer {
    public TerrainSample latestSample;
    private float viewportWidth, viewportHeight;
    private RegionEvaluatorFunction eval;
    public Vector2i latestWinner;

    public LocalSayleServer(float fovX, float fovY, RegionEvaluatorFunction eval) {
        this.viewportWidth = 2 * (float) Math.tan(fovX / 2);
        this.viewportHeight = 2 * (float) Math.tan(fovY / 2);
        this.eval = eval;
    }

    public Vector2f desiredInput(RaycastInfo[][] sample) {
        latestSample = new TerrainSample(sample, viewportWidth, viewportHeight);

        int centerX = latestSample.width / 2;
        int centerY = latestSample.height / 2;

        double bestScore = 0;
        Vector2f bestDirection = new Vector2f(0, 0);

        // Iterate over all cells in the sample grid
        for (int x = 0; x < sample.length; x++) {
            for (int y = 0; y < sample[x].length; y++) {
                double score = latestSample.pathScores[y][x];

                if (score > bestScore) {
                    bestScore = score;
                    bestDirection = new Vector2f(x - centerX, -(y - centerY));
                    if (bestDirection.lengthSquared() > 0.01f) {
                        bestDirection = bestDirection.normalize();
                    }

                    latestWinner = new Vector2i(x, y);
                }
            }
        }

        return bestDirection;
    }

    private double clampDistance(double distance) {
        return 1 - Math.exp(-distance);
    }

}
