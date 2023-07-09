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

import java.util.Arrays;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
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

    private final class ProcessorVertex {
        public final float x;
        public final float y;
        public final float z;
        public final float u;
        public final float v;
        public final float nx;
        public final float ny;
        public final float nz;
        public final float r;
        public final float g;
        public final float b;
        public final float a;
        public final float cx;
        public final float cy;
        public final float cz;
        public final float cw;
        public final float cwinv;
        public final float cxw;
        public final float cyw;
        public final float czw;
        public final boolean clip;
        
        public ProcessorVertex(int vertex) {
            this.u = VertexProcessor.this.localVertices[vertex + 3];
            this.v = VertexProcessor.this.localVertices[vertex + 4];
            this.r = VertexProcessor.this.localVertices[vertex + 8];
            this.g = VertexProcessor.this.localVertices[vertex + 9];
            this.b = VertexProcessor.this.localVertices[vertex + 10];
            this.a = VertexProcessor.this.localVertices[vertex + 11];
            
            float vx = VertexProcessor.this.localVertices[vertex + 0];
            float vy = VertexProcessor.this.localVertices[vertex + 1];
            float vz = VertexProcessor.this.localVertices[vertex + 2];
            
            float vnx = VertexProcessor.this.localVertices[vertex + 5];
            float vny = VertexProcessor.this.localVertices[vertex + 6];
            float vnz = VertexProcessor.this.localVertices[vertex + 7];

            Vector4f pos = new Vector4f(vx, vy, vz, 1f);
            VertexProcessor.this.model.transform(pos);
            this.x = pos.x();
            this.y = pos.y();
            this.z = pos.z();
            
            VertexProcessor.this.projectionView.transform(pos);
            
            this.cx = pos.x();
            this.cy = pos.y();
            this.cz = pos.z();
            this.cw = pos.w();

            this.cwinv = 1f / this.cw;

            this.cxw = this.cx * this.cwinv;
            this.cyw = this.cy * this.cwinv;
            this.czw = this.cz * this.cwinv;
            
            Vector3f N = new Vector3f(vnx, vny, vnz);
            VertexProcessor.this.normalModel.transform(N).normalize();
            this.nx = N.x();
            this.ny = N.y();
            this.nz = N.z();
            
            this.clip = this.cxw > 1f || this.cxw < -1f || this.cyw > 1f || this.cyw < -1f || this.czw > 1f || this.czw < -1f;
        }
    }

    public VertexProcessor(float[] vertices, Matrix4fc projection, Matrix4fc view, Vector3fc position, Matrix4fc model, boolean billboard) {
        this.localVertices = vertices;
        if (projection != null) {
            this.projectionView.set(projection);
        }
        if (view != null) {
            this.projectionView.mul(view);
        }
        if (position != null) {
            this.projectionView.mul(new Matrix4f().translate(-position.x(), -position.y(), -position.z()));
        }
        if (model != null) {
            this.model.set(model);
        }
        if (billboard && view != null) {
            this.model.mul(view.invert(new Matrix4f()));
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
        for (int i = 0; i < (this.localVertices.length / (VertexBuilder.LOCAL_VERTEX_SIZE * 3)); i++) {
            int v0i = i * (VertexBuilder.LOCAL_VERTEX_SIZE * 3);
            int v1i = v0i + VertexBuilder.LOCAL_VERTEX_SIZE;
            int v2i = v1i + VertexBuilder.LOCAL_VERTEX_SIZE;

            ProcessorVertex v0 = new ProcessorVertex(v0i);
            ProcessorVertex v1 = new ProcessorVertex(v1i);
            ProcessorVertex v2 = new ProcessorVertex(v2i);
            
            if (v0.clip || v1.clip || v2.clip) {
                //https://read.cash/@Metalhead33/software-renderer-4-complex-shapes-z-buffers-alpha-blending-perspective-correction-cameras-c1ebfd00

                int verticesIndexStore = this.verticesIndex;
                float[] verticesStore = this.vertices;

                this.verticesIndex = 0;
                this.vertices = new float[PROCESSED_VERTEX_SIZE * 36];

                vertex(v0);
                vertex(v1);
                vertex(v2);

                int[] inputList = new int[36];
                int inputListIndex = 0;
                int[] outputList = new int[36];
                int outputListIndex = 0;

                inputList[0] = 0;
                inputList[1] = 1;
                inputList[2] = 2;
                inputListIndex += 3;

                for (Vector4f clippingEdge : VertexProcessor.clippingEdges) {
                    if (inputListIndex < 3) {
                        continue;
                    }
                    outputListIndex = 0;
                    int idxPrev = inputList[0];
                    //inputList, not output
                    //outputList.add(idxPrev);
                    inputList[inputListIndex] = idxPrev;
                    inputListIndex++;
                    float dpPrev = (clippingEdge.x() * this.vertices[(idxPrev * PROCESSED_VERTEX_SIZE) + 0]) + (clippingEdge.y() * this.vertices[(idxPrev * PROCESSED_VERTEX_SIZE) + 1]) + (clippingEdge.z() * this.vertices[(idxPrev * PROCESSED_VERTEX_SIZE) + 2]) + (clippingEdge.w() * this.vertices[(idxPrev * PROCESSED_VERTEX_SIZE) + 3]);
                    for (int j = 1; j < inputListIndex; ++j) {
                        int idx = inputList[j];
                        float dp = (clippingEdge.x() * this.vertices[(idx * PROCESSED_VERTEX_SIZE) + 0]) + (clippingEdge.y() * this.vertices[(idx * PROCESSED_VERTEX_SIZE) + 1]) + (clippingEdge.z() * this.vertices[(idx * PROCESSED_VERTEX_SIZE) + 2]) + (clippingEdge.w() * this.vertices[(idx * PROCESSED_VERTEX_SIZE) + 3]);

                        if (dpPrev >= 0) {
                            outputList[outputListIndex] = idxPrev;
                            outputListIndex++;
                        }

                        if (Math.signum(dp) != Math.signum(dpPrev)) {
                            float t = dp < 0 ? dpPrev / (dpPrev - dp) : -dpPrev / (dp - dpPrev);
                            int v = interpolateVertex(idxPrev, idx, 1f - t, t);
                            outputList[outputListIndex] = v;
                            outputListIndex++;
                        }

                        idxPrev = idx;
                        dpPrev = dp;

                    }
                    int[] e = inputList;

                    inputListIndex = outputListIndex;
                    inputList = outputList;
                    outputList = e;
                }

                if (inputListIndex < 3) {
                    this.vertices = verticesStore;
                    this.verticesIndex = verticesIndexStore;
                    continue;
                }

                int[] resultIndices = new int[3 + ((inputListIndex - 3) * 3)];
                int resultIndicesIndex = 0;

                resultIndices[0] = inputList[0];
                resultIndices[1] = inputList[1];
                resultIndices[2] = inputList[2];
                resultIndicesIndex += 3;
                for (int j = 3; j < inputListIndex; j++) {
                    resultIndices[resultIndicesIndex + 0] = inputList[0];
                    resultIndices[resultIndicesIndex + 1] = inputList[j - 1];
                    resultIndices[resultIndicesIndex + 2] = inputList[j];
                    resultIndicesIndex += 3;
                }

                float[] resultVertices = this.vertices;

                this.vertices = verticesStore;
                this.verticesIndex = verticesIndexStore;

                for (int j = 0; j < (resultIndices.length / 3); j++) {
                    int cv0 = resultIndices[(j * 3) + 0];
                    int cv1 = resultIndices[(j * 3) + 1];
                    int cv2 = resultIndices[(j * 3) + 2];

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

            float ccw = (v1.cxw - v0.cxw) * (v2.cyw - v0.cyw) - (v2.cxw - v0.cxw) * (v1.cyw - v0.cyw);
            if (ccw <= 0f) {
                continue;
            }
            
            vertex(v0);
            vertex(v1);
            vertex(v2);
        }

        if ((this.verticesIndex / PROCESSED_VERTEX_SIZE) % 3 != 0) {
            throw new IllegalArgumentException("The stream does not contains triangles. (The number of vertices cannot be divided by 3)");
        }

        return Arrays.copyOf(this.vertices, this.verticesIndex);
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

    private void vertex(ProcessorVertex v) {
        if ((this.verticesIndex + PROCESSED_VERTEX_SIZE) > this.vertices.length) {
            this.vertices = Arrays.copyOf(this.vertices, (this.vertices.length * 2) + PROCESSED_VERTEX_SIZE);
        }

        //camera position
        this.vertices[this.verticesIndex + 0] = v.cx;
        this.vertices[this.verticesIndex + 1] = v.cy;
        this.vertices[this.verticesIndex + 2] = v.cz;
        this.vertices[this.verticesIndex + 3] = v.cw;

        //world position
        this.vertices[this.verticesIndex + 4] = v.x;
        this.vertices[this.verticesIndex + 5] = v.y;
        this.vertices[this.verticesIndex + 6] = v.z;

        //uv
        this.vertices[this.verticesIndex + 7] = v.u;
        this.vertices[this.verticesIndex + 8] = v.v;

        //normal
        this.vertices[this.verticesIndex + 9] = v.nx;
        this.vertices[this.verticesIndex + 10] = v.ny;
        this.vertices[this.verticesIndex + 11] = v.nz;

        //color
        this.vertices[this.verticesIndex + 12] = v.r;
        this.vertices[this.verticesIndex + 13] = v.g;
        this.vertices[this.verticesIndex + 14] = v.b;
        this.vertices[this.verticesIndex + 15] = v.a;

        this.verticesIndex += PROCESSED_VERTEX_SIZE;
    }

}
