package com.yallo.sayle.sandbox.game;

import com.yallo.sayle.core.RaycastInfo;
import com.yallo.sayle.core.RaycastableTerrain;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class ObstacleCourse implements RaycastableTerrain {
    List<SolidBox> boxes;
    ArrayList<Vector3f> debugLines;

    public ObstacleCourse(List<SolidBox> boxes) {
        this.boxes = boxes;
        debugLines = new ArrayList<>();
    }

    @Override
    public RaycastInfo raycast(Vector3f start, Vector3f dir) {
        return boxes.stream()
                .map(box -> box.raycast(start, dir))
                .reduce(RaycastInfo::min)
                .orElse(new RaycastInfo(Float.POSITIVE_INFINITY, false));
    }

    public void draw(Camera camera) {
        for (var box : boxes) {
            box.draw(camera);
        }

        for (int i = 0; i < debugLines.size(); i++) {
            glBegin(GL_LINES);
            glColor3f(1.0f, 0.0f, 0.0f);

            glVertex3f(debugLines.get(i).x, debugLines.get(i).y, debugLines.get(i).z);
            i++;
            glVertex3f(debugLines.get(i).x, debugLines.get(i).y, debugLines.get(i).z);

            glEnd();
        }

        debugLines.clear();
    }
}
