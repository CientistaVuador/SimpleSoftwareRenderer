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
import org.joml.Vector3f;

/**
 *
 * @author Cien
 */
public class VertexBuilder {

    //local position, uv, normal, color
    public static final int LOCAL_VERTEX_SIZE = 3 + 2 + 3 + 4;

    private float[] positions = new float[64];
    private int positionsIndex = 0;

    private float[] uvs = new float[64];
    private int uvsIndex = 0;

    private float[] normals = new float[64];
    private int normalsIndex = 0;

    private float[] colors = new float[64];
    private int colorsIndex = 0;

    private float[] vertices = new float[LOCAL_VERTEX_SIZE * 32];
    private int verticesIndex = 0;

    public VertexBuilder() {

    }

    public int position(float x, float y, float z) {
        if ((this.positionsIndex + 3) > this.positions.length) {
            this.positions = Arrays.copyOf(this.positions, this.positions.length * 2);
        }

        this.positions[this.positionsIndex + 0] = x;
        this.positions[this.positionsIndex + 1] = y;
        this.positions[this.positionsIndex + 2] = z;

        this.positionsIndex += 3;

        return (this.positionsIndex / 3);
    }

    public int texture(float u, float v) {
        if ((this.uvsIndex + 2) > this.uvs.length) {
            this.uvs = Arrays.copyOf(this.uvs, this.uvs.length * 2);
        }

        this.uvs[this.uvsIndex + 0] = u;
        this.uvs[this.uvsIndex + 1] = v;

        this.uvsIndex += 2;

        return (this.uvsIndex / 2);
    }

    public int normal(float nX, float nY, float nZ) {
        if ((this.normalsIndex + 3) > this.normals.length) {
            this.normals = Arrays.copyOf(this.normals, this.normals.length * 2);
        }

        this.normals[this.normalsIndex + 0] = nX;
        this.normals[this.normalsIndex + 1] = nY;
        this.normals[this.normalsIndex + 2] = nZ;

        this.normalsIndex += 3;

        return (this.normalsIndex / 3);
    }

    public int color(float r, float g, float b, float a) {
        if ((this.colorsIndex + 4) > this.colors.length) {
            this.colors = Arrays.copyOf(this.colors, this.colors.length * 2);
        }

        this.colors[this.colorsIndex + 0] = r;
        this.colors[this.colorsIndex + 1] = g;
        this.colors[this.colorsIndex + 2] = b;
        this.colors[this.colorsIndex + 3] = a;

        this.colorsIndex += 4;

        return (this.colorsIndex / 4);
    }

    public void vertex(int positionIndex, int textureIndex, int normalIndex, int colorIndex) {
        float x = 0f;
        float y = 0f;
        float z = 0f;
        if (positionIndex != 0) {
            positionIndex--;

            x = this.positions[(positionIndex * 3) + 0];
            y = this.positions[(positionIndex * 3) + 1];
            z = this.positions[(positionIndex * 3) + 2];
        }

        float u = 0f;
        float v = 0f;
        if (textureIndex != 0) {
            textureIndex--;

            u = this.uvs[(textureIndex * 2) + 0];
            v = this.uvs[(textureIndex * 2) + 1];
        }

        float nX = Float.NaN;
        float nY = Float.NaN;
        float nZ = Float.NaN;
        if (normalIndex != 0) {
            normalIndex--;

            nX = this.normals[(normalIndex * 3) + 0];
            nY = this.normals[(normalIndex * 3) + 1];
            nZ = this.normals[(normalIndex * 3) + 2];
        }

        float r = 1f;
        float g = 1f;
        float b = 1f;
        float a = 1f;
        if (colorIndex != 0) {
            colorIndex--;

            r = this.colors[(colorIndex * 4) + 0];
            g = this.colors[(colorIndex * 4) + 1];
            b = this.colors[(colorIndex * 4) + 2];
            a = this.colors[(colorIndex * 4) + 3];
        }

        if ((this.verticesIndex + LOCAL_VERTEX_SIZE) > this.vertices.length) {
            this.vertices = Arrays.copyOf(this.vertices, (this.vertices.length * 2) + LOCAL_VERTEX_SIZE);
        }

        //local position
        this.vertices[this.verticesIndex + 0] = x;
        this.vertices[this.verticesIndex + 1] = y;
        this.vertices[this.verticesIndex + 2] = z;

        //uv
        this.vertices[this.verticesIndex + 3] = u;
        this.vertices[this.verticesIndex + 4] = v;

        //normal
        this.vertices[this.verticesIndex + 5] = nX;
        this.vertices[this.verticesIndex + 6] = nY;
        this.vertices[this.verticesIndex + 7] = nZ;

        //color
        this.vertices[this.verticesIndex + 8] = r;
        this.vertices[this.verticesIndex + 9] = g;
        this.vertices[this.verticesIndex + 10] = b;
        this.vertices[this.verticesIndex + 11] = a;

        this.verticesIndex += LOCAL_VERTEX_SIZE;

        if ((this.verticesIndex / LOCAL_VERTEX_SIZE) % 3 == 0) {
            int v2 = this.verticesIndex - LOCAL_VERTEX_SIZE;
            int v1 = v2 - LOCAL_VERTEX_SIZE;
            int v0 = v1 - LOCAL_VERTEX_SIZE;

            float v0nx = this.vertices[v0 + 5];
            float v0ny = this.vertices[v0 + 6];
            float v0nz = this.vertices[v0 + 7];

            float v1nx = this.vertices[v1 + 5];
            float v1ny = this.vertices[v1 + 6];
            float v1nz = this.vertices[v1 + 7];

            float v2nx = this.vertices[v2 + 5];
            float v2ny = this.vertices[v2 + 6];
            float v2nz = this.vertices[v2 + 7];

            if (flatShaded(v0nx, v0ny, v0nz) || flatShaded(v1nx, v1ny, v1nz) || flatShaded(v2nx, v2ny, v2nz)) {
                float v0x = this.vertices[v0 + 0];
                float v0y = this.vertices[v0 + 1];
                float v0z = this.vertices[v0 + 2];

                float v1x = this.vertices[v1 + 0];
                float v1y = this.vertices[v1 + 1];
                float v1z = this.vertices[v1 + 2];

                float v2x = this.vertices[v2 + 0];
                float v2y = this.vertices[v2 + 1];
                float v2z = this.vertices[v2 + 2];

                Vector3f A = new Vector3f();
                Vector3f B = new Vector3f();
                Vector3f N = new Vector3f();

                A.set(v1x, v1y, v1z).sub(v0x, v0y, v0z);
                B.set(v2x, v2y, v2z).sub(v0x, v0y, v0z);
                N.set(A).cross(B).normalize();

                v0nx = N.x();
                v0ny = N.y();
                v0nz = N.z();
                
                this.vertices[v0 + 5] = v0nx;
                this.vertices[v0 + 6] = v0ny;
                this.vertices[v0 + 7] = v0nz;
                
                this.vertices[v1 + 5] = v0nx;
                this.vertices[v1 + 6] = v0ny;
                this.vertices[v1 + 7] = v0nz;
                
                this.vertices[v2 + 5] = v0nx;
                this.vertices[v2 + 6] = v0ny;
                this.vertices[v2 + 7] = v0nz;
            }
        }
    }

    private boolean flatShaded(float x, float y, float z) {
        return Float.isNaN(x) || Float.isNaN(y) || Float.isNaN(z);
    }

    public float[] vertices() {
        if ((this.verticesIndex / LOCAL_VERTEX_SIZE) % 3 != 0) {
            throw new IllegalArgumentException("The stream does not contains triangles. (The number of vertices cannot be divided by 3)");
        }

        return Arrays.copyOf(this.vertices, this.verticesIndex);
    }

}
