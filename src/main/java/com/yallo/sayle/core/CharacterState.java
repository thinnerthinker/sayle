package com.yallo.sayle.core;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class CharacterState {
    public Vector3f position;
    public float velocity;
    public Vector3f forward, up, right;

    public CharacterState(Vector3f position, float velocity) {
        this.position = position;
        this.velocity = velocity;

        this.forward = new Vector3f(0f, 0f, -1f);
        this.up = new Vector3f(0f, 1f, 0f);
        this.right = new Vector3f(1f, 0f, 0f);
    }
}
