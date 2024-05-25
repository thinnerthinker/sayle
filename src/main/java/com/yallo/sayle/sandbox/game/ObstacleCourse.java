package com.yallo.sayle.sandbox.game;

import com.yallo.sayle.core.RaycastInfo;
import com.yallo.sayle.core.RaycastableTerrain;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class ObstacleCourse implements RaycastableTerrain {
    public List<SolidBox> boxes;

    public ObstacleCourse(List<SolidBox> boxes) {
        this.boxes = boxes;
    }

    @Override
    public RaycastInfo raycast(Vector3f start, Vector3f dir) {
        return boxes.stream()
                .map(box -> box.raycast(start, dir))
                .reduce(RaycastInfo::min)
                .orElse(new RaycastInfo(Float.POSITIVE_INFINITY, false, new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                        new Vector3f(0, 0, 1)));
    }

    public void draw(Camera camera) {
        for (var box : boxes) {
            box.draw(camera);
        }
    }
}
