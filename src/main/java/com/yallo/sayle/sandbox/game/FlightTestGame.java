package com.yallo.sayle.sandbox.game;

import com.yallo.sayle.core.CharacterState;
import com.yallo.sayle.core.FlightBehavior;
import com.yallo.sayle.core.RegionEvaluatorFunction;
import com.yallo.sayle.sandbox.input.Input;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Random;

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
        float fovX = 45f * (float)Math.PI / 180f, fovY = 45f * (float)Math.PI / 180f;
        int sampleSize = 50;

        camera = new Camera(0.25f, 0.15f, 10f);
        character = new Character(new CharacterState(new Vector3f(0f, 0f, 0f),
                10f),
                0.5f);

        ArrayList<SolidBox> obstacles = createHoles();

        course = new ObstacleCourse(obstacles);

        flight = new FlightBehavior(sampleSize,0.5f, fovX, fovY);
        costFunction = RegionEvaluatorFunction.safest(fovX, fovY, sampleSize);
    }

    public ArrayList<SolidBox> createHoles() {
        ArrayList<SolidBox> obstacles = new ArrayList<>();
        Random random = new Random();

        float holeSize = 3, holeDeviation = 6;
        float borderSize = 50; // The half-size of each SolidBox

        /* obstacles.add(new SolidBox(new Vector3f(-borderSize - 1, -borderSize - 1, -30), new Vector3f(-borderSize, borderSize, -100)));
        obstacles.add(new SolidBox(new Vector3f(borderSize, -borderSize - 1, -30), new Vector3f(borderSize + 1, borderSize, -100)));
        obstacles.add(new SolidBox(new Vector3f(-borderSize - 1, -borderSize - 1, -30), new Vector3f(borderSize, -borderSize, -100)));
        obstacles.add(new SolidBox(new Vector3f(-borderSize, borderSize, -30), new Vector3f(borderSize, borderSize + 1, -100))); */

        for (float z = -30; z >= -300; z -= 30) {
            float holeX = random.nextFloat() * holeDeviation - holeDeviation / 2;
            float holeY = random.nextFloat() * holeDeviation - holeDeviation / 2;

            obstacles.add(new SolidBox(new Vector3f(-borderSize, -borderSize, z), new Vector3f(holeX, borderSize, z + 1)));
            obstacles.add(new SolidBox(new Vector3f(holeX + holeSize, -borderSize, z), new Vector3f(borderSize, borderSize, z + 1)));
            obstacles.add(new SolidBox(new Vector3f(holeX, -borderSize, z), new Vector3f(holeX + holeSize, holeY, z + 1)));
            obstacles.add(new SolidBox(new Vector3f(holeX, holeY + holeSize, z), new Vector3f(holeX + holeSize, borderSize, z + 1)));
        }

        return obstacles;
    }

    boolean isMouseInWindow() {
        var pos = Input.getMousePosition();
        return pos.x >= 0 && pos.x < Window.getWidth() && pos.y >= 0 && pos.y < Window.getHeight();
    }

    @Override
    public void update(double dt) {
        if (Input.isKeyPressed(GLFW_KEY_SPACE)) {

            mouseCaptured = !mouseCaptured;

            if (mouseCaptured) {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            } else {
                glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }
        }

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
