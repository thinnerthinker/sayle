package com.yallo.sayle.core;

import org.joml.Vector2f;

public interface SayleServer {
    Vector2f desiredInput(RaycastInfo[][] sample);
}
