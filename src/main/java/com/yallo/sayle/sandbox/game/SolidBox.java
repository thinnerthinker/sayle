package com.yallo.sayle.sandbox.game;

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
                0, 0, -1, // Front face
                0, 0, 1,  // Back face
                -1, 0, 0, // Left face
                1, 0, 0,  // Right face
                0, -1, 0, // Bottom face
                0, 1, 0   // Top face
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

    public RaycastInfo raycast(Vector3f rayStart, Vector3f rayDir) {
        Vector3f dirfrac = new Vector3f();
        dirfrac.x = 1.0f / rayDir.x;
        dirfrac.y = 1.0f / rayDir.y;
        dirfrac.z = 1.0f / rayDir.z;

        float t1 = (min.x - rayStart.x) * dirfrac.x;
        float t2 = (max.x - rayStart.x) * dirfrac.x;
        float t3 = (min.y - rayStart.y) * dirfrac.y;
        float t4 = (max.y - rayStart.y) * dirfrac.y;
        float t5 = (min.z - rayStart.z) * dirfrac.z;
        float t6 = (max.z - rayStart.z) * dirfrac.z;

        float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        // if tmax < 0, ray (line) is intersecting AABB, but the whole AABB is behind us
        if (tmax < 0) {
            return new RaycastInfo(Float.POSITIVE_INFINITY, false);
        }

        // if tmin > tmax, ray doesn't intersect AABB
        if (tmin > tmax) {
            return new RaycastInfo(Float.POSITIVE_INFINITY, false);
        }

        return new RaycastInfo(tmin, true);
    }


    public void draw(Camera camera) {
        Shader shader = Resources.getTerrainShader();
        shader.bind();

        shader.setUniform("viewProj", camera.getViewProjection());
        //shader.setUniform("viewProj", new Matrix4f());
        mesh.draw();

        shader.unbind();
    }
}
