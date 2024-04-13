package com.yallo.sayle.sandbox.game;

import com.yallo.sayle.core.CharacterState;
import com.yallo.sayle.core.FlightBehavior;
import com.yallo.sayle.core.RaycastInfo;
import com.yallo.sayle.core.RegionEvaluatorFunction;
import com.yallo.sayle.sandbox.input.Input;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class FlightTestGame extends Game {
    private Camera camera;
    private boolean mouseCaptured;

    private Character character;
    private ObstacleCourse course;

    private FlightBehavior flight;
    private RegionEvaluatorFunction costFunction;

    private RaycastInfo[][] lastSample;



    @Override
    public void initialize() {
        float fovX = 45f * (float)Math.PI / 180f, fovY = 45f * (float)Math.PI / 180f;
        int sampleSize = 20;

        camera = new Camera(0.25f, 0.10f, 10f);
        character = new Character(new CharacterState(new Vector3f(0f, 0f, 0f),
                20f),
                0.5f);

        ArrayList<SolidBox> obstacles = createHoles(-30, -30, 3);
        course = new ObstacleCourse(obstacles);

        flight = new FlightBehavior(sampleSize,0.5f, fovX, fovY);
        costFunction = RegionEvaluatorFunction.safest(fovX, fovY, sampleSize);

        glfwSetInputMode(flightWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public ArrayList<SolidBox> createHoles(float startZ, float stepZ, float count) {
        ArrayList<SolidBox> obstacles = new ArrayList<>();
        Random random = new Random();

        float holeSize = 3, holeDeviation = 3, holeDepth = 15;
        float borderSize = 50;

        for (int i = 0; i < count; i++) {
            float z = startZ + i * stepZ;

            float holeX = random.nextFloat() * holeDeviation - holeDeviation / 2;
            float holeY = random.nextFloat() * holeDeviation - holeDeviation / 2;

            obstacles.add(new SolidBox(new Vector3f(-borderSize, -borderSize, z), new Vector3f(holeX, borderSize, z + holeDepth)));
            obstacles.add(new SolidBox(new Vector3f(holeX + holeSize, -borderSize, z), new Vector3f(borderSize, borderSize, z + holeDepth)));
            obstacles.add(new SolidBox(new Vector3f(holeX, -borderSize, z), new Vector3f(holeX + holeSize, holeY, z + holeDepth)));
            obstacles.add(new SolidBox(new Vector3f(holeX, holeY + holeSize, z), new Vector3f(holeX + holeSize, borderSize, z + holeDepth)));
        }

        return obstacles;
    }

    boolean isMouseInWindow() {
        var pos = Input.getMousePosition();
        return pos.x >= 0 && pos.x < Window.getWidth() && pos.y >= 0 && pos.y < Window.getHeight();
    }

    @Override
    public void update(double dt) {
        if (Input.isKeyDown(GLFW_KEY_SPACE)) {
            mouseCaptured = !mouseCaptured;

            if (mouseCaptured) {
                glfwSetInputMode(flightWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            } else {
                glfwSetInputMode(flightWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }
        }

        lastSample = flight.getDepthField(character.state.clone(), course);

        camera.setPivotPosition(character.state.position);
        camera.update((float) dt);

        if (character.state.position.z < course.boxes.get(0).min.z) {
            course.boxes.remove(0);
            course.boxes.remove(0);
            course.boxes.remove(0);
            course.boxes.remove(0);

            course.boxes.addAll(createHoles(character.state.position.z - 3 * 30, -30, 1));
        }

        //throw new RuntimeException();
    }

    @Override
    public void updateQuantized(double dt) {
        lastSample = flight.quantizedDepthField(lastSample);

        Vector2f input = flight.desiredInput(lastSample, costFunction);
        character.pushInput(input, (float) dt);
        character.state.position.add(new Vector3f(character.state.forward).mul(character.state.velocity * (float) dt));

        character.updateTransform();
    }

    @Override
    public void draw() {
        character.draw(camera);
        course.draw(camera);
    }

    @Override
    public void drawDepthField() {
        if (lastSample == null) return;

        int rows = lastSample.length;
        int cols = lastSample[0].length;

        float squareWidth = 2.0f / cols; // Adjusted for NDC
        float squareHeight = 2.0f / rows; // Adjusted for NDC

        float minDistance = Float.POSITIVE_INFINITY;
        float maxDistance = Float.NEGATIVE_INFINITY;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                float distance = lastSample[y][x].distance;
                if (distance < minDistance) minDistance = distance;
                if (distance > maxDistance) maxDistance = distance;
            }
        }

        // Check if minDistance and maxDistance are too close
        if (Math.abs(maxDistance - minDistance) < 1e-5) {
            minDistance = 0;
            maxDistance = 1;
        }

        if (Float.isInfinite(maxDistance)) {
            maxDistance = 50;
        }

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                float distance = lastSample[y][x].distance;
                float colorIntensity;
                //if (distance)
                colorIntensity = (distance - minDistance) / (maxDistance - minDistance);
                glColor3f(colorIntensity, colorIntensity, colorIntensity);

                float posX = -1 + x * squareWidth; // Adjusted for NDC
                float posY = -1 + y * squareHeight; // Adjusted for NDC

                glBegin(GL_QUADS);
                glVertex2f(posX, posY);
                glVertex2f(posX + squareWidth, posY);
                glVertex2f(posX + squareWidth, posY + squareHeight);
                glVertex2f(posX, posY + squareHeight);
                glEnd();
            }
        }
    }

    @Override
    public void dispose() {

    }
}
