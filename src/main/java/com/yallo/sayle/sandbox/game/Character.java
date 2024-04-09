package com.yallo.sayle.sandbox.game;

import com.yallo.sayle.core.CharacterState;
import com.yallo.sayle.sandbox.resources.Resources;
import com.yallo.sayle.sandbox.resources.Shader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Character {
    public CharacterState state;
    public float angleX, angleY;
    public float rotationSpeed;

    private Matrix4f model;

    private SolidBox display;

    public Character(CharacterState state, float rotationSpeed) {
        this.state = state;
        this.rotationSpeed = rotationSpeed;

        angleX = 0;
        angleY = 0;

        float widthH = 0.4f, heightH = 0.9f;
        display = new SolidBox(new Vector3f(-widthH, -widthH, -heightH), new Vector3f(widthH, widthH, heightH));

        updateTransform();
    }

    public void pushInput(Vector2f mouseMovement, float dt) {
        //System.out.println(mouseMovement.y);

        angleX -= rotationSpeed * mouseMovement.x * dt;
        angleY -= rotationSpeed * mouseMovement.y * dt;

        updateTransform();
    }

    public void updateTransform() {
        Matrix4f rotation = new Matrix4f().rotationYXZ(angleX, angleY, 0);
        state.forward = rotation.transformDirection(new Vector3f(0, 0, -1));

        Vector3f originalUp = new Vector3f(0.0f, 1.0f, 0.0f);

        state.up = rotation.transformDirection(originalUp);
        state.right = new Vector3f(state.forward).cross(state.up);

        model = new Matrix4f().translate(state.position).mul(rotation);
    }

    public void draw(Camera camera) {
        Shader shader = Resources.getCharacterShader();
        shader.bind();

        shader.setUniform("viewProj", camera.getViewProjection().mul(model));
        display.mesh.draw();

        shader.unbind();
    }
}
