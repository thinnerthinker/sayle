package com.yallo.sayle.sandbox.game;

import com.yallo.sayle.core.Parameters;
import com.yallo.sayle.core.RaycastInfo;
import com.yallo.sayle.sandbox.resources.Mesh;
import com.yallo.sayle.sandbox.resources.Resources;
import com.yallo.sayle.sandbox.resources.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SolidBox {
    public Vector3f min, max;
    public Mesh mesh;

    public SolidBox(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;

        float[] vertices = {
                min.x, min.y, min.z,
                max.x, min.y, min.z,
                max.x, max.y, min.z,
                min.x, max.y, min.z,

                min.x, min.y, max.z,
                max.x, min.y, max.z,
                max.x, max.y, max.z,
                min.x, max.y, max.z,
        };

        // Define normals for each face
        float[] normals = {
                // Front face (min.z)
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,
                0.0f, 0.0f, -1.0f,

                // Back face (max.z)
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,

                // Left face (min.x)
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,
                -1.0f, 0.0f, 0.0f,

                // Right face (max.x)
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,
                1.0f, 0.0f, 0.0f,

                // Bottom face (min.y)
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,
                0.0f, -1.0f, 0.0f,

                // Top face (max.y)
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f
        };

        int[] indices = {
                0, 2, 1, 0, 3, 2,  // Front face
                4, 5, 6, 4, 6, 7,  // Back face
                0, 4, 7, 0, 7, 3,  // Left face
                1, 6, 5, 1, 2, 6,  // Right face
                0, 1, 5, 0, 5, 4,  // Bottom face
                3, 6, 2, 3, 7, 6   // Top face
        };

        // Create the mesh
        mesh = new Mesh(vertices, normals, indices);
    }

    public static SolidBox fromExtents(Vector3f center, float hwidth, float hheight, float hdepth) {
        Vector3f min = new Vector3f(center.x - hwidth, center.y - hheight, center.z - hdepth);
        Vector3f max = new Vector3f(center.x + hwidth, center.y + hheight, center.z + hdepth);
        return new SolidBox(min, max);
    }

    public RaycastInfo raycast(Vector3f rayStart, Vector3f rayDir) {
        Vector3f dirfrac = new Vector3f(1.0f / rayDir.x, 1.0f / rayDir.y, 1.0f / rayDir.z);

        float d1 = min.x - rayStart.x;
        float d2 = max.x - rayStart.x;
        float d3 = min.y - rayStart.y;
        float d4 = max.y - rayStart.y;
        float d5 = min.z - rayStart.z;
        float d6 = max.z - rayStart.z;

        float t1 = d1 * dirfrac.x;
        float t2 = d2 * dirfrac.x;
        float t3 = d3 * dirfrac.y;
        float t4 = d4 * dirfrac.y;
        float t5 = d5 * dirfrac.z;
        float t6 = d6 * dirfrac.z;

        float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if (tmax < 0 || tmin > tmax) {
            return new RaycastInfo(Parameters.veryFar, false, new Vector3f(rayStart).add(new Vector3f(rayDir).mul(Parameters.veryFar)),
                    new Vector3f(0, 0, 0));  // No intersection
        }

        int signX = rayDir.x < 0 ? -1 : 1, signY = rayDir.y < 0 ? 1 : -1, signZ = rayDir.z < 0 ? 1 : -1;
        Vector3f normal = new Vector3f(0, 0, 0);

        if (tmin == t1 || tmin == t2) normal.x = signX ;
        else if (tmin == t3 || tmin == t4) normal.y = signY;
        else normal.z = signZ;

        float steepnessX = rayDir.x * signX;
        float steepnessY = rayDir.y * signY;
        float steepnessZ = rayDir.z * signZ;

        float maxSteepness = Math.max(steepnessX, Math.max(steepnessY, steepnessZ));

        float straightDistance;
        if (maxSteepness == steepnessX) straightDistance = Math.min(Math.abs(d1), Math.abs(d2));
        else if (maxSteepness == steepnessY) straightDistance = Math.min(Math.abs(d3), Math.abs(d4));
        else straightDistance = Math.min(Math.abs(d5), Math.abs(d6));

        return new RaycastInfo(tmin, true, new Vector3f(rayStart).add(new Vector3f(rayDir).mul(tmin)), normal);
    }


    public void draw(Camera camera) {
        Shader shader = Resources.getTerrainShader();
        shader.bind();

        shader.setUniform("viewProj", camera.getViewProjection());
        shader.setUniform("cameraPos", camera.getPivotPosition());
        //shader.setUniform("viewProj", new Matrix4f());
        mesh.draw();

        shader.unbind();
    }
}
