package com.yallo.sayle.core;

import org.joml.Vector3f;

import java.util.Objects;

public class RaycastInfo {
    public float distance;
    public Vector3f normal;
    public boolean solid;

    public RaycastInfo(float distance, boolean solid, Vector3f normal) {
        this.distance = distance;
        this.solid = solid;
        this.normal = normal;
    }

    public RaycastInfo(RaycastInfo that) {
        this.distance = that.distance;
        this.solid = that.solid;
        this.normal = that.normal;
    }

    public RaycastInfo min(RaycastInfo other) {
        return distance < other.distance ? this : other;
    }
    public RaycastInfo max(RaycastInfo other) {
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
