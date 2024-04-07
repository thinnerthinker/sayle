package com.yallo.sayle.core;

import java.util.Objects;

public class RaycastInfo {
    public float distance;
    public boolean solid;

    public RaycastInfo(float distance, boolean solid) {
        this.distance = distance;
        this.solid = solid;
    }

    public RaycastInfo() {
    }

    public RaycastInfo min(RaycastInfo other) {
        return distance < other.distance ? this : other;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RaycastInfo that = (RaycastInfo) o;
        return Float.compare(distance, that.distance) == 0 && solid == that.solid;
    }

    public boolean similar(RaycastInfo that, float tolerance) {
        return (Float.isInfinite(distance) && Float.isInfinite(that.distance)) ||
                (Math.abs(distance - that.distance) < tolerance && solid == that.solid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(distance, solid);
    }
}
