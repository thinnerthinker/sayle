package com.yallo.sayle.core;

public class Parameters {
    public static float fovX = 60f * (float)Math.PI / 180f, fovY = 60f * (float)Math.PI / 180f;
    public static int sampleSize = 100;
    public static int quantizationBuckets = 2;
    public static float coveringSafeDistance = 0.5f, coveringDistanceTolerance = 0.01f;
    public static float veryFar = 1000;
}
