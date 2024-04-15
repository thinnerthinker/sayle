package com.yallo.sayle.core;

import org.joml.Vector2f;

import java.util.Comparator;

public class LocalSayleServer implements SayleServer {
    public TerrainSample latestSample;
    private float viewportWidth, viewportHeight;
    private RegionEvaluatorFunction eval;
    public TerrainSampleRegion latestWinner;

    public LocalSayleServer(float fovX, float fovY, RegionEvaluatorFunction eval) {
        this.viewportWidth = 2 * (float) Math.tan(fovX / 2);
        this.viewportHeight = 2 * (float) Math.tan(fovY / 2);
        this.eval = eval;
    }

    @Override
    public Vector2f desiredInput(RaycastInfo[][] sample) {
        latestSample = new TerrainSample(sample, viewportWidth, viewportHeight);

        var bestEval = latestSample.getRegions().stream().map(eval::calculate).min(Comparator.comparing(e -> e.cost)).get();
        latestWinner = bestEval.region;

        return bestEval.suggestedDirection;
    }
}
