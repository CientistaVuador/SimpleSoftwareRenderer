/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */
package cientistavuador.simplesoftwarerenderer.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 *
 * @author Cien
 */
public class VertexProcessor {

    //camera position, world position, uv, world normal, color
    public static final int PROCESSED_VERTEX_SIZE = 4 + 3 + 2 + 3 + 4;

    private static final Vector4f[] clippingEdges = new Vector4f[]{
        new Vector4f(-1, 0, 0, 1),
        new Vector4f(1, 0, 0, 1),
        new Vector4f(0, -1, 0, 1),
        new Vector4f(0, 1, 0, 1),
        new Vector4f(0, 0, -1, 1),
        new Vector4f(0, 0, 1, 1)
    };

    private final float[] localVertices;
    private final Matrix4f projectionView = new Matrix4f();
    private final Matrix4f model = new Matrix4f();
    private final Matrix3f normalModel = new Matrix3f();

    private float[] vertices = new float[PROCESSED_VERTEX_SIZE * 64];
    private int verticesIndex = 0;

    public VertexProcessor(float[] vertices, Matrix4f projectionView, Matrix4f model) {
        this.localVertices = vertices;
        if (projectionView != null) {
            this.projectionView.set(projectionView);
        }
        if (model != null) {
            this.model.set(model);
        }
        this.normalModel.set(new Matrix4f(this.model).invert().transpose());
    }

    public float[] getLocalVertices() {
        return localVertices;
    }

    public Matrix4fc getProjectionView() {
        return projectionView;
    }

    public Matrix4fc getModel() {
        return model;
    }

    public Matrix3fc getNormalModel() {
        return normalModel;
    }

    public float[] process() {
        this.verticesIndex = 0;

        Vector3f N = new Vector3f();

        Vector4f cacheA = new Vector4f();
        Vector4f cacheB = new Vector4f();

        for (int i = 0; i < (this.localVertices.length / (VertexBuilder.LOCAL_VERTEX_SIZE * 3)); i++) {
            int v0 = i * (VertexBuilder.LOCAL_VERTEX_SIZE * 3);
            int v1 = v0 + VertexBuilder.LOCAL_VERTEX_SIZE;
            int v2 = v1 + VertexBuilder.LOCAL_VERTEX_SIZE;

            //0
            float v0x = this.localVertices[v0 + 0];
            float v0y = this.localVertices[v0 + 1];
            float v0z = this.localVertices[v0 + 2];
            float v0u = this.localVertices[v0 + 3];
            float v0v = this.localVertices[v0 + 4];
            float v0nx = this.localVertices[v0 + 5];
            float v0ny = this.localVertices[v0 + 6];
            float v0nz = this.localVertices[v0 + 7];
            float v0r = this.localVertices[v0 + 8];
            float v0g = this.localVertices[v0 + 9];
            float v0b = this.localVertices[v0 + 10];
            float v0a = this.localVertices[v0 + 11];

            cacheB.set(v0x, v0y, v0z, 1f);
            this.model.transformProject(cacheB);
            v0x = cacheB.x();
            v0y = cacheB.y();
            v0z = cacheB.z();

            cacheA.set(v0x, v0y, v0z, 1f);
            this.projectionView.transform(cacheA);

            float v0cx = cacheA.x();
            float v0cy = cacheA.y();
            float v0cz = cacheA.z();
            float v0cw = cacheA.w();

            float v0cwinv = 1f / v0cw;

            float v0cxw = v0cx * v0cwinv;
            float v0cyw = v0cy * v0cwinv;
            float v0czw = v0cz * v0cwinv;
            //

            //1
            float v1x = this.localVertices[v1 + 0];
            float v1y = this.localVertices[v1 + 1];
            float v1z = this.localVertices[v1 + 2];
            float v1u = this.localVertices[v1 + 3];
            float v1v = this.localVertices[v1 + 4];
            float v1nx = this.localVertices[v1 + 5];
            float v1ny = this.localVertices[v1 + 6];
            float v1nz = this.localVertices[v1 + 7];
            float v1r = this.localVertices[v1 + 8];
            float v1g = this.localVertices[v1 + 9];
            float v1b = this.localVertices[v1 + 10];
            float v1a = this.localVertices[v1 + 11];

            cacheB.set(v1x, v1y, v1z, 1f);
            this.model.transformProject(cacheB);
            v1x = cacheB.x();
            v1y = cacheB.y();
            v1z = cacheB.z();

            cacheA.set(v1x, v1y, v1z, 1f);
            this.projectionView.transform(this.model.transform(cacheA));

            float v1cx = cacheA.x();
            float v1cy = cacheA.y();
            float v1cz = cacheA.z();
            float v1cw = cacheA.w();

            float v1cwinv = 1f / v1cw;

            float v1cxw = v1cx * v1cwinv;
            float v1cyw = v1cy * v1cwinv;
            float v1czw = v1cz * v1cwinv;
            //

            //2
            float v2x = this.localVertices[v2 + 0];
            float v2y = this.localVertices[v2 + 1];
            float v2z = this.localVertices[v2 + 2];
            float v2u = this.localVertices[v2 + 3];
            float v2v = this.localVertices[v2 + 4];
            float v2nx = this.localVertices[v2 + 5];
            float v2ny = this.localVertices[v2 + 6];
            float v2nz = this.localVertices[v2 + 7];
            float v2r = this.localVertices[v2 + 8];
            float v2g = this.localVertices[v2 + 9];
            float v2b = this.localVertices[v2 + 10];
            float v2a = this.localVertices[v2 + 11];

            cacheB.set(v2x, v2y, v2z, 1f);
            this.model.transformProject(cacheB);
            v2x = cacheB.x();
            v2y = cacheB.y();
            v2z = cacheB.z();

            cacheA.set(v2x, v2y, v2z, 1f);
            this.projectionView.transform(this.model.transform(cacheA));

            float v2cx = cacheA.x();
            float v2cy = cacheA.y();
            float v2cz = cacheA.z();
            float v2cw = cacheA.w();

            float v2cwinv = 1f / v2cw;

            float v2cxw = v2cx * v2cwinv;
            float v2cyw = v2cy * v2cwinv;
            float v2czw = v2cz * v2cwinv;
            //

            boolean clip0 = clip(v0cxw, v0cyw, v0czw);
            boolean clip1 = clip(v1cxw, v1cyw, v1czw);
            boolean clip2 = clip(v2cxw, v2cyw, v2czw);

            N.set(v0nx, v0ny, v0nz);
            this.normalModel.transform(N).normalize();
            v0nx = N.x();
            v0ny = N.y();
            v0nz = N.z();

            N.set(v1nx, v1ny, v1nz);
            this.normalModel.transform(N).normalize();
            v1nx = N.x();
            v1ny = N.y();
            v1nz = N.z();

            N.set(v2nx, v2ny, v2nz);
            this.normalModel.transform(N).normalize();
            v2nx = N.x();
            v2ny = N.y();
            v2nz = N.z();

            if (clip0 || clip1 || clip2) {
                if (v2nx == v2nx) {
                    continue;
                }
                
                //https://read.cash/@Metalhead33/software-renderer-4-complex-shapes-z-buffers-alpha-blending-perspective-correction-cameras-c1ebfd00
                
                int verticesIndexStore = this.verticesIndex;
                float[] verticesStore = this.vertices;

                this.verticesIndex = 0;
                this.vertices = new float[PROCESSED_VERTEX_SIZE * 36];

                vertex(v0cx, v0cy, v0cz, v0cw, v0x, v0y, v0z, v0u, v0v, v0nx, v0ny, v0nz, v0r, v0g, v0b, v0a);
                vertex(v1cx, v1cy, v1cz, v1cw, v1x, v1y, v1z, v1u, v1v, v1nx, v1ny, v1nz, v1r, v1g, v1b, v1a);
                vertex(v2cx, v2cy, v2cz, v2cw, v2x, v2y, v2z, v2u, v2v, v2nx, v2ny, v2nz, v2r, v2g, v2b, v2a);

                List<Integer> inputList = new ArrayList<>();
                List<Integer> outputList = new ArrayList<>();

                inputList.add(0);
                inputList.add(1);
                inputList.add(2);

                for (Vector4f clippingEdge : VertexProcessor.clippingEdges) {
                    if (inputList.size() < 3) {
                        continue;
                    }
                    outputList.clear();
                    int idxPrev = inputList.get(0);
                    //inputList, not output
                    //outputList.add(idxPrev);
                    inputList.add(idxPrev);
                    float dpPrev = (clippingEdge.x() * this.vertices[(idxPrev * PROCESSED_VERTEX_SIZE) + 0]) + (clippingEdge.y() * this.vertices[(idxPrev * PROCESSED_VERTEX_SIZE) + 1]) + (clippingEdge.z() * this.vertices[(idxPrev * PROCESSED_VERTEX_SIZE) + 2]) + (clippingEdge.w() * this.vertices[(idxPrev * PROCESSED_VERTEX_SIZE) + 3]);
                    for (int j = 1; j < inputList.size(); ++j) {
                        int idx = inputList.get(j);
                        float dp = (clippingEdge.x() * this.vertices[(idx * PROCESSED_VERTEX_SIZE) + 0]) + (clippingEdge.y() * this.vertices[(idx * PROCESSED_VERTEX_SIZE) + 1]) + (clippingEdge.z() * this.vertices[(idx * PROCESSED_VERTEX_SIZE) + 2]) + (clippingEdge.w() * this.vertices[(idx * PROCESSED_VERTEX_SIZE) + 3]);

                        if (dpPrev >= 0) {
                            outputList.add(idxPrev);
                        }

                        if (Math.signum(dp) != Math.signum(dpPrev)) {
                            float t = dp < 0 ? dpPrev / (dpPrev - dp) : -dpPrev / (dp - dpPrev);
                            int v = interpolateVertex(idxPrev, idx, 1f - t, t);
                            outputList.add(v);
                        }

                        idxPrev = idx;
                        dpPrev = dp;
                    }
                    inputList.clear();
                    inputList.addAll(outputList);
                }

                if (inputList.size() < 3) {
                    this.vertices = verticesStore;
                    this.verticesIndex = verticesIndexStore;
                    continue;
                }

                List<Integer> resultIndices = new ArrayList<>();

                resultIndices.add(inputList.get(0));
                resultIndices.add(inputList.get(1));
                resultIndices.add(inputList.get(2));
                for (int j = 3; j < inputList.size(); j++) {
                    resultIndices.add(inputList.get(0));
                    resultIndices.add(inputList.get(j - 1));
                    resultIndices.add(inputList.get(j));
                }

                float[] resultVertices = this.vertices;

                this.vertices = verticesStore;
                this.verticesIndex = verticesIndexStore;

                for (int j = 0; j < (resultIndices.size() / 3); j++) {
                    int cv0 = resultIndices.get((j * 3) + 0);
                    int cv1 = resultIndices.get((j * 3) + 1);
                    int cv2 = resultIndices.get((j * 3) + 2);

                    float cv0cwinv = 1f / resultVertices[(cv0 * PROCESSED_VERTEX_SIZE) + 3];
                    float cv0cxw = resultVertices[(cv0 * PROCESSED_VERTEX_SIZE) + 0] * cv0cwinv;
                    float cv0cyw = resultVertices[(cv0 * PROCESSED_VERTEX_SIZE) + 1] * cv0cwinv;
                    
                    float cv1cwinv = 1f / resultVertices[(cv1 * PROCESSED_VERTEX_SIZE) + 3];
                    float cv1cxw = resultVertices[(cv1 * PROCESSED_VERTEX_SIZE) + 0] * cv1cwinv;
                    float cv1cyw = resultVertices[(cv1 * PROCESSED_VERTEX_SIZE) + 1] * cv1cwinv;
                    
                    float cv2cwinv = 1f / resultVertices[(cv2 * PROCESSED_VERTEX_SIZE) + 3];
                    float cv2cxw = resultVertices[(cv2 * PROCESSED_VERTEX_SIZE) + 0] * cv2cwinv;
                    float cv2cyw = resultVertices[(cv2 * PROCESSED_VERTEX_SIZE) + 1] * cv2cwinv;

                    float ccw = (cv1cxw - cv0cxw) * (cv2cyw - cv0cyw) - (cv2cxw - cv0cxw) * (cv1cyw - cv0cyw);
                    if (ccw <= 0f) {
                        continue;
                    }

                    if ((this.verticesIndex + (PROCESSED_VERTEX_SIZE * 3)) > this.vertices.length) {
                        this.vertices = Arrays.copyOf(this.vertices, (this.vertices.length * 2) + (PROCESSED_VERTEX_SIZE * 3));
                    }

                    System.arraycopy(resultVertices, cv0 * PROCESSED_VERTEX_SIZE, this.vertices, this.verticesIndex + (0 * PROCESSED_VERTEX_SIZE), PROCESSED_VERTEX_SIZE);
                    System.arraycopy(resultVertices, cv1 * PROCESSED_VERTEX_SIZE, this.vertices, this.verticesIndex + (1 * PROCESSED_VERTEX_SIZE), PROCESSED_VERTEX_SIZE);
                    System.arraycopy(resultVertices, cv2 * PROCESSED_VERTEX_SIZE, this.vertices, this.verticesIndex + (2 * PROCESSED_VERTEX_SIZE), PROCESSED_VERTEX_SIZE);

                    this.verticesIndex += (PROCESSED_VERTEX_SIZE * 3);
                }

                continue;
            }

            float ccw = (v1cxw - v0cxw) * (v2cyw - v0cyw) - (v2cxw - v0cxw) * (v1cyw - v0cyw);
            if (ccw <= 0f) {
                continue;
            }

            vertex(v0cx, v0cy, v0cz, v0cw, v0x, v0y, v0z, v0u, v0v, v0nx, v0ny, v0nz, v0r, v0g, v0b, v0a);
            vertex(v1cx, v1cy, v1cz, v1cw, v1x, v1y, v1z, v1u, v1v, v1nx, v1ny, v1nz, v1r, v1g, v1b, v1a);
            vertex(v2cx, v2cy, v2cz, v2cw, v2x, v2y, v2z, v2u, v2v, v2nx, v2ny, v2nz, v2r, v2g, v2b, v2a);
        }

        if ((this.verticesIndex / PROCESSED_VERTEX_SIZE) % 3 != 0) {
            throw new IllegalArgumentException("The stream does not contains triangles. (The number of vertices cannot be divided by 3)");
        }

        return Arrays.copyOf(this.vertices, this.verticesIndex);
    }

    private boolean clip(float x, float y, float z) {
        return x > 1f || x < -1f || y > 1f || y < -1f || z > 1f || z < -1f;
    }

    private int interpolateVertex(int vA, int vB, float w0, float w1) {
        if ((this.verticesIndex + PROCESSED_VERTEX_SIZE) > this.vertices.length) {
            this.vertices = Arrays.copyOf(this.vertices, (this.vertices.length * 2) + PROCESSED_VERTEX_SIZE);
        }

        float[] output = new float[PROCESSED_VERTEX_SIZE];
        for (int i = 0; i < PROCESSED_VERTEX_SIZE; i++) {
            float valueA = this.vertices[(vA * PROCESSED_VERTEX_SIZE) + i];
            float valueB = this.vertices[(vB * PROCESSED_VERTEX_SIZE) + i];
            output[i] = (valueA * w0) + (valueB * w1);
        }
        System.arraycopy(output, 0, this.vertices, this.verticesIndex, output.length);

        this.verticesIndex += PROCESSED_VERTEX_SIZE;

        return (this.verticesIndex / PROCESSED_VERTEX_SIZE) - 1;
    }

    private void vertex(float cX, float cY, float cZ, float cW, float wX, float wY, float wZ, float u, float v, float nX, float nY, float nZ, float r, float g, float b, float a) {
        if ((this.verticesIndex + PROCESSED_VERTEX_SIZE) > this.vertices.length) {
            this.vertices = Arrays.copyOf(this.vertices, (this.vertices.length * 2) + PROCESSED_VERTEX_SIZE);
        }

        //camera position
        this.vertices[this.verticesIndex + 0] = cX;
        this.vertices[this.verticesIndex + 1] = cY;
        this.vertices[this.verticesIndex + 2] = cZ;
        this.vertices[this.verticesIndex + 3] = cW;

        //world position
        this.vertices[this.verticesIndex + 4] = wX;
        this.vertices[this.verticesIndex + 5] = wY;
        this.vertices[this.verticesIndex + 6] = wZ;

        //uv
        this.vertices[this.verticesIndex + 7] = u;
        this.vertices[this.verticesIndex + 8] = v;

        //normal
        this.vertices[this.verticesIndex + 9] = nX;
        this.vertices[this.verticesIndex + 10] = nY;
        this.vertices[this.verticesIndex + 11] = nZ;

        //color
        this.vertices[this.verticesIndex + 12] = r;
        this.vertices[this.verticesIndex + 13] = g;
        this.vertices[this.verticesIndex + 14] = b;
        this.vertices[this.verticesIndex + 15] = a;

        this.verticesIndex += PROCESSED_VERTEX_SIZE;
    }

}
