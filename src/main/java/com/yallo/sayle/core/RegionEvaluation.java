package com.yallo.sayle.core;

import org.joml.Vector2f;

public class RegionEvaluation {
    public float cost;
    public Vector2f suggestedPoint;

    public RegionEvaluation(float cost, Vector2f suggestedPoint) {
        this.cost = cost;
        this.suggestedPoint = suggestedPoint;
    }
}
