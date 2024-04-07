package com.yallo.sayle.sandbox.game;

import com.yallo.sayle.core.CharacterState;
import com.yallo.sayle.core.FlightBehavior;
import com.yallo.sayle.core.RegionCostFunction;
import com.yallo.sayle.sandbox.input.*;
import com.yallo.sayle.sandbox.input.Input;
import com.yallo.sayle.sandbox.resources.*;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;

public class FlightTestGame extends Game {
    private Camera camera;

    private Character character;
    private ObstacleCourse course;

    private FlightBehavior flight;
    private RegionCostFunction costFunction;

    @Override
    public void initialize() {
        float fovX = 45f, fovY = 45f;
        int sampleSize = 100;

        camera = new Camera(0.25f, 0.15f, 10f);
        character = new Character(new CharacterState(new Vector3f(0f, 0f, 0f),
                1f),
                0.15f);

        ArrayList<SolidBox> obstacles = new ArrayList<>();
        obstacles.add(new SolidBox(new Vector3f(-10f, -10f, -12f), new Vector3f(5f, 5f, -10f)));

        course = new ObstacleCourse(obstacles);

        flight = new FlightBehavior(sampleSize,2f, fovX, fovY);
        costFunction = RegionCostFunction.safest(fovX, fovY, sampleSize);
    }

    boolean isMouseInWindow() {
        var pos = Input.getMousePosition();
        return pos.x >= 0 && pos.x < Window.getWidth() && pos.y >= 0 && pos.y < Window.getHeight();
    }

    @Override
    public void update(double dt) {
        character.pushInput(flight.desiredInput(character.state, course, costFunction), (float) dt);
        character.state.position.add(character.state.forward.mul(character.state.velocity * (float) dt));

        camera.setPivotPosition(character.state.position);
        camera.update((float) dt);
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
