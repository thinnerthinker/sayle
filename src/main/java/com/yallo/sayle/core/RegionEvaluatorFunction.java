package com.yallo.sayle.core;

import org.joml.Vector2f;

/**
 * Costs are usually bounded to some finite range that depends on the score function.
 * Some cost functions have special cases that are extra important, which may lie in different ranges than the rest.
 */
@FunctionalInterface
public interface RegionEvaluatorFunction {
    RegionEvaluation calculate(TerrainSampleRegion region);

    static RegionEvaluatorFunction safest(float fovX, float fovY, int sampleSize) {
        float viewportHalfWidth = (float) Math.tan(fovX), viewportHalfHeight = (float) Math.tan(fovY);
        float aspectRatio = viewportHalfWidth / viewportHalfHeight;

        float sampleWidth = aspectRatio * sampleSize, sampleHeight = sampleSize;
        float maxDistance = new Vector2f(sampleWidth, sampleHeight).length() / 2f;

        Vector2f viewportCenter = new Vector2f(sampleWidth / 2f, sampleHeight / 2f);

        return region -> {
            Vector2f p = region.closestPointTowards(viewportCenter);
            float d = viewportCenter.distance(p) / maxDistance;
            /*if (!region.original.solid) {
                d -= 10;
            }*/

            Vector2f dir = new Vector2f(0, 0);
            if (!p.equals(viewportCenter)) {
                //p.add(region.getCenter().sub(viewportCenter).normalize(safeDistanceFromBorders));
                //p = region.getCenter().sub(viewportCenter);

                dir = region.getCenter().sub(viewportCenter).normalize();
                dir.y *= -1;
            }

            final float weight = 0.0f;
            float distFromAxisZ = 0; // (float) Math.pow(new Vector2f(region.original.position.x, region.original.position.y).lengthSquared(), 0.1);

            /*if (distFromAxisZ < Math.pow(10, 1.0)) {
                distFromAxisZ = 0;
            }*/

            //return new RegionEvaluation(region, region.width * region.height, region.getCenter(), dir);
            return new RegionEvaluation(region, weight * d + 1 / region.distance + distFromAxisZ, region.getCenter(), dir);
        };
    }
}