package com.yallo.sayle.core;

import org.joml.Vector3f;

public interface RaycastableTerrain {
    RaycastInfo raycast(Vector3f start, Vector3f dir);
}
