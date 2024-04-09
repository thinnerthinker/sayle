package com.yallo.sayle.sandbox.game;

import com.yallo.sayle.sandbox.input.Input;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private float angleX, angleY;
    private float rotationSpeed, moveSpeed;
    private float distance;
    private Vector3f pivotPosition;

    private Vector3f up, right, forward;

    private Matrix4f view;
    private final Matrix4f projection;

    private final float aspect;
    private final float nearPlane;

    public Camera(float moveSpeed, float rotationSpeed, float distance) {
        angleX = 0;
        angleY = 0;

        this.moveSpeed = moveSpeed;
        this.rotationSpeed = rotationSpeed;

        this.distance = distance;

        pivotPosition = new Vector3f(0.0f, 0.0f, 0f);

        aspect = 16.0f / 9.0f;
        nearPlane = 0.001f;

        projection = new Matrix4f().perspective((float) Math.PI / 4, aspect, nearPlane, 300.f);
        updateView();
    }

    public void update(float dt) {
        //if (Input.isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
        pan(dt);
        //}
    }

    private void pan(float dt) {
        Vector2f dPos = Input.getMouseMovement();
        angleX -= rotationSpeed * dPos.x * dt;
        //if ((angleY < Math.PI / 2 && rotationSpeed * dPos.y < 0) || (angleY > -Math.PI / 2 && rotationSpeed * dPos.y > 0))
        angleY -= rotationSpeed * dPos.y * dt;

        updateView();
    }

    private void updateView() {
        Matrix4f rotation = new Matrix4f().rotationYXZ(angleX, angleY, 0);

        Vector3f posOnSphere = rotation.transformDirection(new Vector3f(0, 0, 1)).mul(distance);
        Vector3f position = pivotPosition.add(posOnSphere);

        Vector3f originalTarget = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f rotatedTarget = rotation.transformDirection(originalTarget);
        Vector3f finalTarget = new Vector3f(position).add(rotatedTarget);

        forward = rotation.transformDirection(new Vector3f(0, 0, -1));

        Vector3f originalUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f rotatedUp = rotation.transformDirection(originalUp);

        up = rotatedUp;
        right = new Vector3f(forward).cross(up);

        view = new Matrix4f().lookAt(position, finalTarget, rotatedUp);
    }

    public Vector3f getPosition() {
        Matrix4f rotation = new Matrix4f().rotationYXZ(angleX, angleY, 0);

        Vector3f posOnSphere = rotation.transformDirection(new Vector3f(0, 0, 1)).mul(distance);
        return pivotPosition.add(posOnSphere);
    }

    public Vector3f unproject(Vector2f screen) {
        return getViewProjection().invert().transformDirection(new Vector3f(screen, 2*nearPlane)).normalize();
    }

    public Vector3f getUp() {
        return up;
    }

    public Vector3f getRight() {
        return right;
    }

    public Vector3f getForward() {
        return forward;
    }

    public Vector3f getPivotPosition() {
        return pivotPosition;
    }

    public void setPivotPosition(Vector3f pivotPosition) {
        this.pivotPosition = new Vector3f(pivotPosition);
    }

    public Matrix4f getViewProjection() {
        return new Matrix4f().mul(projection).mul(view);
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public float getAspect() {
        return aspect;
    }
}
