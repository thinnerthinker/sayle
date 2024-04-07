package com.yallo.sayle.sandbox.game;

import com.yallo.sayle.core.CharacterState;
import com.yallo.sayle.core.FlightBehavior;
import com.yallo.sayle.core.RegionEvaluatorFunction;
import com.yallo.sayle.sandbox.input.Input;
import org.joml.Vector3f;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class FlightTestGame extends Game {
    private Camera camera;
    private boolean mouseCaptured;

    private Character character;
    private ObstacleCourse course;

    private FlightBehavior flight;
    private RegionEvaluatorFunction costFunction;

    @Override
    public void initialize() {
        float fovX = 25f * (float)Math.PI / 180f, fovY = 25f * (float)Math.PI / 180f;
        int sampleSize = 20;

        camera = new Camera(0.25f, 0.5f, 10f);
        character = new Character(new CharacterState(new Vector3f(0f, 0f, 0f),
                10f),
                0.15f);

        ArrayList<SolidBox> obstacles = new ArrayList<>();
        obstacles.add(new SolidBox(new Vector3f(-10f, -20f, -52f), new Vector3f(2f, 5f, -50f)));
        obstacles.add(new SolidBox(new Vector3f(-20f, -20f, -72f), new Vector3f(10f, 20f, -70f)));

        course = new ObstacleCourse(obstacles);

        flight = new FlightBehavior(sampleSize,20f, fovX, fovY);
        costFunction = RegionEvaluatorFunction.safest(fovX, fovY, sampleSize);
    }

    boolean isMouseInWindow() {
        var pos = Input.getMousePosition();
        return pos.x >= 0 && pos.x < Window.getWidth() && pos.y >= 0 && pos.y < Window.getHeight();
    }

    @Override
    public void update(double dt) {
        if (!Input.isKeyDown(GLFW_KEY_SPACE)) {
            return;
            /*mouseCaptured = !mouseCaptured;

            if (mouseCaptured) {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            } else {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }*/
        }

        //flight.desiredInput(character.state.clone(), course, costFunction);
        character.pushInput(flight.desiredInput(character.state.clone(), course, costFunction), (float) dt);
        character.state.position.add(new Vector3f(character.state.forward).mul(character.state.velocity * (float) dt));
        character.updateTransform();

        camera.setPivotPosition(character.state.position);
        camera.update((float) dt);

        //throw new RuntimeException();
    }

    @Override
    public void draw() {
        character.draw(camera);
        course.draw(camera);
    }

    @Override
    public void dispose() {

    }
}
