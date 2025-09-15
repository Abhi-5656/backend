package com.wfm.experts.util;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class EmbeddingConverter {

    public static byte[] toByteArray(float[] floatArray) {
        if (floatArray == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(floatArray.length * 4);
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        floatBuffer.put(floatArray);
        return byteBuffer.array();
    }

    public static float[] toFloatArray(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        float[] floatArray = new float[floatBuffer.limit()];
        floatBuffer.get(floatArray);
        return floatArray;
    }
}