package com.yallo.sayle.sandbox.game;

import com.yallo.sayle.core.*;
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

    private SayleServer server;
    private SayleClient flight;


    @Override
    public void initialize() {
        camera = new Camera(0.25f, 0.10f, 10f);
        character = new Character(new CharacterState(new Vector3f(0f, 0f, 0f),
                20f),
                1f);

        ArrayList<SolidBox> obstacles = createHoles(-30, -30, 3);
        course = new ObstacleCourse(obstacles);


        server = new LocalSayleServer(Parameters.fovX, Parameters.fovY, RegionEvaluatorFunction.safest(Parameters.fovX, Parameters.fovY, Parameters.sampleSize));
        flight = new SayleClient(Parameters.sampleSize, Parameters.fovX, Parameters.fovY, server);

        glfwSetInputMode(flightWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        flight.desiredInput(character.state, course);
    }

    public ArrayList<SolidBox> createHoles(float startZ, float stepZ, float count) {
        ArrayList<SolidBox> obstacles = new ArrayList<>();
        Random random = new Random();

        float holeSize = 5, holeDeviation = 8, holeDepth = 5;
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
        if (Input.isKeyPressed(GLFW_KEY_SPACE)) {
            mouseCaptured = !mouseCaptured;

            if (mouseCaptured) {
                glfwSetInputMode(flightWindow, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            } else {
                glfwSetInputMode(flightWindow, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }
        }

        Vector2f input = flight.desiredInput(character.state, course);

        character.pushInput(input, (float) dt);
        character.state.position.add(new Vector3f(character.state.forward).mul(character.state.velocity * (float) dt));

        character.updateTransform();

        camera.setPivotPosition(character.state.position);
        camera.update((float) dt);

        if (character.state.position.z < course.boxes.get(0).min.z) {
            course.boxes.remove(0);
            course.boxes.remove(0);
            course.boxes.remove(0);
            course.boxes.remove(0);

            course.boxes.addAll(createHoles(character.state.position.z - 3 * 30, -30, 1));
        }
        if (character.state.position.z < -2000) {
            character.state.position.z += 2000;

            for (var box : course.boxes) {
                box.min.z += 2000;
                box.max.z += 2000;
            }
        }



    }

    @Override
    public void draw() {
        character.draw(camera);
        course.draw(camera);
    }

    public void drawDepthField(float[][] sample) {
        if (sample == null) return;

        int rows = sample.length;
        int cols = sample[0].length;

        float squareWidth = 2.0f / cols;
        float squareHeight = 2.0f / rows;

        float minDistance = Float.POSITIVE_INFINITY;
        float maxDistance = Float.NEGATIVE_INFINITY;

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                float distance = Math.min(sample[y][x], 50);
                if (distance < minDistance) minDistance = distance;
                if (distance > maxDistance) maxDistance = distance;
            }
        }

        // Check if minDistance and maxDistance are too close
        if (Math.abs(maxDistance - minDistance) < 1e-5) {
            minDistance = 0;
            maxDistance = 1;
        }

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {

                float distance = Math.min(sample[y][x], 50);

                float colorIntensity;
                //if (distance)
                colorIntensity = (distance - minDistance) / (maxDistance - minDistance);
                glColor3f(colorIntensity, colorIntensity, colorIntensity);

                float posX = -(-1 + x * squareWidth); // Adjusted for NDC
                float posY = -(-1 + y * squareHeight); // Adjusted for NDC

                glBegin(GL_QUADS);
                glVertex2f(posX, posY);
                glVertex2f(posX - squareWidth, posY);
                glVertex2f(posX - squareWidth, posY - squareHeight);
                glVertex2f(posX, posY - squareHeight);
                glEnd();
            }
        }

    }

    @Override
    public void drawRawDepthField() {
        TerrainSample terrainSample = ((LocalSayleServer)server).latestSample;
        float[][] sample = new float[terrainSample.height][terrainSample.width];

        for (int y = 0; y < terrainSample.height; y++) {
            for (int x = 0; x < terrainSample.width; x++) {
                sample[y][x] = terrainSample.raw[y][x].distance;
            }
        }

        drawDepthField(sample);
    }

    @Override
    public void drawQuantizedDepthField() {
        drawDepthField(((LocalSayleServer)server).latestSample.quantized);
    }

    @Override
    public void drawCoveredDepthField() {
        drawDepthField(((LocalSayleServer)server).latestSample.covered);
    }

    @Override
    public void drawRegions() {
        drawDepthField(((LocalSayleServer)server).latestSample.covered);

        var sample = ((LocalSayleServer) server).latestSample;
        var regions = ((LocalSayleServer) server).latestSample.regions;
        var bestRegion = ((LocalSayleServer) server).latestWinner;

        float colorBump = 1f / (regions.size() - 1);
        float color = 0;

        Random random = new Random(12);

        glBegin(GL_QUADS);
        for (var region : regions) {
            if (region == bestRegion) {
                glColor3f(1, 0 ,0);
            } else {
                glColor3f(color, color, color);
            }

            color += colorBump;

            float squareWidth = 2.0f / sample.width;
            float squareHeight = 2.0f / sample.height;

            float posX = -(-1 + region.position.x * squareWidth);
            float posY = -(-1 + region.position.y * squareHeight);

            squareWidth *= region.width;
            squareHeight *= region.height;

            glVertex2f(posX, posY);
            glVertex2f(posX - squareWidth, posY);
            glVertex2f(posX - squareWidth, posY - squareHeight);
            glVertex2f(posX, posY - squareHeight);
        }
        glEnd();
    }

    @Override
    public void dispose() {

    }
}
