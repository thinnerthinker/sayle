package com.yallo.sayle.core;

import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

        var evals = latestSample.getRegions().stream().map(eval::calculate).collect(Collectors.toList());
        List<List<RegionEvaluation>> prioritizedEvals = new ArrayList<>();

        for (int i = 0; i < evals.get(0).cost.length; i++) {
            final int index = i;
            prioritizedEvals.add(evals.stream().sorted(Comparator.comparingDouble(a -> a.cost[index])).collect(Collectors.toList()));
        }

        var bestRegion = evals.stream()
                .min(Comparator.comparingDouble(
                        eval -> IntStream.range(0, prioritizedEvals.size())
                                .mapToDouble(i -> prioritizedEvals.get(i).indexOf(eval) / (double)(1 << i))
                                .sum())).get();

        latestWinner = bestRegion.region;
        return bestRegion.suggestedDirection;
    }
}
