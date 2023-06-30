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
public class VerticesProcessor {

    //camera position / w, inverse w, world position / w, uv / w, world normal / w, color / w
    public static final int PROCESSED_VERTEX_SIZE = 3 + 1 + 3 + 2 + 3 + 4;
    
    private static final Vector4f[] clippingEdges = new Vector4f[] {
        new Vector4f(-1, 0, 0, 1),
        new Vector4f(1, 0, 0, 1),
        new Vector4f(0, -1, 0, 1),
        new Vector4f(0, 1, 0, 1),
        new Vector4f(0, 0, -1, 1),
        new Vector4f(0, 0, 1, 1),
    };
    
    private final float[] localVertices;
    private final Matrix4f projectionView = new Matrix4f();
    private final Matrix4f model = new Matrix4f();
    private final Matrix3f normalModel = new Matrix3f();

    private float[] vertices = new float[PROCESSED_VERTEX_SIZE * 64];
    private int verticesIndex = 0;

    public VerticesProcessor(float[] vertices, Matrix4f projectionView, Matrix4f model) {
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

        for (int i = 0; i < (this.localVertices.length / (VerticesBuilder.LOCAL_VERTEX_SIZE * 3)); i++) {
            int v0 = i * (VerticesBuilder.LOCAL_VERTEX_SIZE * 3);
            int v1 = v0 + VerticesBuilder.LOCAL_VERTEX_SIZE;
            int v2 = v1 + VerticesBuilder.LOCAL_VERTEX_SIZE;

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

            if (clip0 && clip1 && clip2) {
                continue;
            }

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
                //todo
                continue;
            }
            
            float ccw = (v1cxw - v0cxw) * (v2cyw - v0cyw) - (v2cxw - v0cxw) * (v1cyw - v0cyw);
            if (ccw <= 0f) {
                continue;
            }
            
            vertex(v0cxw, v0cyw, v0czw, v0cwinv, v0x, v0y, v0z, v0u, v0v, v0nx, v0ny, v0nz, v0r, v0g, v0b, v0a);
            vertex(v1cxw, v1cyw, v1czw, v1cwinv, v1x, v1y, v1z, v1u, v1v, v1nx, v1ny, v1nz, v1r, v1g, v1b, v1a);
            vertex(v2cxw, v2cyw, v2czw, v2cwinv, v2x, v2y, v2z, v2u, v2v, v2nx, v2ny, v2nz, v2r, v2g, v2b, v2a);
        }

        if ((this.verticesIndex / PROCESSED_VERTEX_SIZE) % 3 != 0) {
            throw new IllegalArgumentException("The stream does not contains triangles. (The number of vertices cannot be divided by 3)");
        }

        return Arrays.copyOf(this.vertices, this.verticesIndex);
    }

    private boolean clip(float x, float y, float z) {
        return x > 1f || x < -1f || y > 1f || y < -1f || z > 1f || z < -1f;
    }

    private void vertex(float cXW, float cYW, float cZW, float invcW, float wX, float wY, float wZ, float u, float v, float nX, float nY, float nZ, float r, float g, float b, float a) {
        if ((this.verticesIndex + PROCESSED_VERTEX_SIZE) > this.vertices.length) {
            this.vertices = Arrays.copyOf(this.vertices, (this.vertices.length * 2) + PROCESSED_VERTEX_SIZE);
        }
        
        //camera position
        this.vertices[this.verticesIndex + 0] = ((cXW + 1f) * 0.5f);
        this.vertices[this.verticesIndex + 1] = ((cYW + 1f) * 0.5f);
        this.vertices[this.verticesIndex + 2] = ((cZW + 1f) * 0.5f) * invcW;
        
        //inverse w
        this.vertices[this.verticesIndex + 3] = invcW;
        
        //world position
        this.vertices[this.verticesIndex + 4] = wX * invcW;
        this.vertices[this.verticesIndex + 5] = wY * invcW;
        this.vertices[this.verticesIndex + 6] = wZ * invcW;

        //uv
        this.vertices[this.verticesIndex + 7] = u * invcW;
        this.vertices[this.verticesIndex + 8] = v * invcW;

        //normal
        this.vertices[this.verticesIndex + 9] = nX * invcW;
        this.vertices[this.verticesIndex + 10] = nY * invcW;
        this.vertices[this.verticesIndex + 11] = nZ * invcW;

        //color
        this.vertices[this.verticesIndex + 12] = r * invcW;
        this.vertices[this.verticesIndex + 13] = g * invcW;
        this.vertices[this.verticesIndex + 14] = b * invcW;
        this.vertices[this.verticesIndex + 15] = a * invcW;

        this.verticesIndex += PROCESSED_VERTEX_SIZE;
    }

}
