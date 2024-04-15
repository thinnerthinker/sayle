package com.yallo.sayle.core;

import org.joml.Vector2f;

public class RegionEvaluation {
    public float cost;
    public Vector2f suggestedPoint, suggestedDirection;
    public TerrainSampleRegion region;

    public RegionEvaluation(TerrainSampleRegion region, float cost, Vector2f suggestedPoint, Vector2f suggestedDirection) {
        this.region = region;
        this.cost = cost;
        this.suggestedPoint = suggestedPoint;
        this.suggestedDirection = suggestedDirection;
    }
}
