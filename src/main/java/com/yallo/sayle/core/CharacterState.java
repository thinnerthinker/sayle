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

    public CharacterState(Vector3f position, float velocity, Vector3f forward, Vector3f up, Vector3f right) {
        this.position = position;
        this.velocity = velocity;

        this.forward = new Vector3f(forward);
        this.up = new Vector3f(up);
        this.right = new Vector3f(right);
    }

    public CharacterState clone() {
        return new CharacterState(new Vector3f(position), velocity, forward, up, right);
    }
}
