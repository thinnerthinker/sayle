package com.yallo.sayle.sandbox.game;

import com.yallo.sayle.core.RaycastInfo;
import com.yallo.sayle.sandbox.resources.Mesh;
import com.yallo.sayle.sandbox.resources.Resources;
import com.yallo.sayle.sandbox.resources.Shader;
import org.joml.Vector3f;

public class SolidBox {
    public Vector3f min, max;
    public Mesh mesh;

    public SolidBox(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;

        float[] vertices = {
            min.x, min.y, min.z, // 0
            max.x, min.y, min.z, // 1
            max.x, max.y, min.z, // 2
            min.x, max.y, min.z, // 3
            min.x, min.y, max.z, // 4
            max.x, min.y, max.z, // 5
            max.x, max.y, max.z, // 6
            min.x, max.y, max.z  // 7
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
            0, 1, 2,  0, 2, 3,  // Front face
            4, 5, 6,  4, 6, 7,  // Back face
            0, 4, 7,  0, 7, 3,  // Left face
            1, 5, 6,  1, 6, 2,  // Right face
            0, 1, 5,  0, 5, 4,  // Bottom face
            3, 2, 6,  3, 6, 7   // Top face
        };

        // Create the mesh
        mesh = new Mesh(vertices, normals, indices);
    }

    private RaycastInfo rayPlaneX(Vector3f rayStart, Vector3f rayDir)
    {
        // rayStart.x + t * rayDir.x = closestPlaneX
        float closestPlane = rayStart.x - min.x < 0 ? min.x : max.x;
        float t = (closestPlane - rayStart.x) / rayDir.x;

        Vector3f p = rayStart.add(rayDir.mul(t));

        if (!(t > 0 && Math.abs(p.y - min.y) <= (max.y - min.y) / 2f && Math.abs(p.z - min.z) <= (max.z - min.z) / 2f)) {
            return new RaycastInfo(Float.POSITIVE_INFINITY, false);
        }
        return new RaycastInfo(t, true);
    }
    private RaycastInfo rayPlaneY(Vector3f rayStart, Vector3f rayDir)
    {
        float closestPlane = rayStart.y - min.y < 0 ? min.y : max.y;
        float t = (closestPlane - rayStart.y) / rayDir.y;

        Vector3f p = rayStart.add(rayDir.mul(t));

        if (!(t > 0 && Math.abs(p.x - min.x) <= (max.x - min.x) / 2f && Math.abs(p.z - min.z) <= (max.z - min.z) / 2f)) {
            new RaycastInfo(Float.POSITIVE_INFINITY, false);
        }
        return new RaycastInfo(t, true);
    }
    private RaycastInfo rayPlaneZ(Vector3f rayStart, Vector3f rayDir)
    {
        float closestPlane = rayStart.z - min.z < 0 ? min.z : max.z;
        float t = (closestPlane - rayStart.z) / rayDir.z;

        Vector3f p = rayStart.add(rayDir.mul(t));

        if (!(t > 0 && Math.abs(p.x - min.x) <= (max.x - min.x) / 2f && Math.abs(p.y - min.y) <= (max.y - min.y) / 2f)) {
            return new RaycastInfo(Float.POSITIVE_INFINITY, false);
        }
        return new RaycastInfo(t, true);
    }

    public RaycastInfo raycast(Vector3f rayStart, Vector3f rayDir)
    {
        return rayPlaneX(rayStart, rayDir).min(rayPlaneY(rayStart, rayDir)).min(rayPlaneY(rayStart, rayDir));
    }

    public void draw(Camera camera) {
        Shader shader = Resources.getTerrainShader();
        shader.bind();

        shader.setUniform("viewProj", camera.getViewProjection());
        mesh.draw();

        shader.unbind();
    }
}
