package com.yallo.sayle.core;

import org.joml.Vector2f;

public class RegionEvaluation {
    public float cost;
    public Vector2f suggestedPoint, suggestedDirection;

    public RegionEvaluation(float cost, Vector2f suggestedPoint, Vector2f suggestedDirection) {
        this.cost = cost;
        this.suggestedPoint = suggestedPoint;
        this.suggestedDirection = suggestedDirection;
    }
}
