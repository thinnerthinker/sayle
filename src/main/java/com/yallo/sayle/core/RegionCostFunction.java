package com.yallo.sayle.core;

import org.joml.Vector2f;

/**
 * Costs are usually bounded to some finite range that depends on the score function.
 * Some cost functions have special cases that are extra important, which may lie in different ranges than the rest.
 */
@FunctionalInterface
public interface RegionCostFunction {
    float calculate(TerrainSampleRegion region);

    private static float distanceFromViewportCenter(Vector2f position, int sampleWidth, int sampleHeight) {
        return new Vector2f(position.x - sampleWidth / 2f, position.y - sampleHeight / 2f).length();
    }

    static RegionCostFunction safest(float minDepth, float maxDepth, int sampleWidth, int sampleHeight) {
        float maxDistance = new Vector2f(sampleWidth, sampleHeight).length() / 2f;

        return region -> {
            float d = RegionCostFunction.distanceFromViewportCenter(region.getCenter(), sampleWidth, sampleHeight) / maxDistance;
            final float weight = 1;

            return weight * d + 1 / region.info.distance;
        };
    }
}