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

import org.joml.Vector3fc;

/**
 *
 * @author Cien
 */
public class Rasterizer {

    private final Surface surface;
    private final Texture texture;
    private final float[] vertices;
    private final Vector3fc lightDirection;
    private final Vector3fc lightDiffuse;
    private final Vector3fc lightAmbient;
    private final boolean depthOnly;
    private final boolean bilinear;

    public Rasterizer(Surface surface, Texture texture, float[] vertices, Vector3fc lightDirection, Vector3fc lightDiffuse, Vector3fc lightAmbient, boolean depthOnly, boolean bilinear) {
        this.surface = surface;
        this.texture = texture;
        this.vertices = vertices;
        this.lightDirection = lightDirection;
        this.lightDiffuse = lightDiffuse;
        this.lightAmbient = lightAmbient;
        this.depthOnly = depthOnly;
        this.bilinear = bilinear;
    }

    public Vector3fc getLightDirection() {
        return lightDirection;
    }

    public Vector3fc getLightDiffuse() {
        return lightDiffuse;
    }

    public Vector3fc getLightAmbient() {
        return lightAmbient;
    }

    public Surface getSurface() {
        return surface;
    }

    public Texture getTexture() {
        return texture;
    }

    public float[] getVertices() {
        return vertices;
    }

    public boolean isDepthOnly() {
        return depthOnly;
    }

    public boolean isBilinear() {
        return bilinear;
    }
    
    public void render() {
        float[] color = new float[3];
        float[] textureColor = new float[4];
        int width = this.surface.getWidth();
        int height = this.surface.getHeight();
        for (int i = 0; i < (this.vertices.length / (VertexProcessor.PROCESSED_VERTEX_SIZE * 3)); i++) {
            int v0 = i * (VertexProcessor.PROCESSED_VERTEX_SIZE * 3);
            int v1 = v0 + VertexProcessor.PROCESSED_VERTEX_SIZE;
            int v2 = v1 + VertexProcessor.PROCESSED_VERTEX_SIZE;

            //0
            float v0cwinv = 1f / this.vertices[v0 + 3];

            float v0cx = ((this.vertices[v0 + 0] * v0cwinv) + 1.0f) * 0.5f * width;
            float v0cy = ((this.vertices[v0 + 1] * v0cwinv) + 1.0f) * 0.5f * height;
            float v0cz = ((this.vertices[v0 + 2] * v0cwinv) + 1.0f) * 0.5f;
            float v0x = this.vertices[v0 + 4] * v0cwinv;
            float v0y = this.vertices[v0 + 5] * v0cwinv;
            float v0z = this.vertices[v0 + 6] * v0cwinv;
            float v0u = this.vertices[v0 + 7] * v0cwinv;
            float v0v = this.vertices[v0 + 8] * v0cwinv;
            float v0nx = this.vertices[v0 + 9] * v0cwinv;
            float v0ny = this.vertices[v0 + 10] * v0cwinv;
            float v0nz = this.vertices[v0 + 11] * v0cwinv;
            float v0r = this.vertices[v0 + 12] * v0cwinv;
            float v0g = this.vertices[v0 + 13] * v0cwinv;
            float v0b = this.vertices[v0 + 14] * v0cwinv;
            float v0a = this.vertices[v0 + 15] * v0cwinv;
            //

            //1
            float v1cwinv = 1f / this.vertices[v1 + 3];

            float v1cx = ((this.vertices[v1 + 0] * v1cwinv) + 1.0f) * 0.5f * width;
            float v1cy = ((this.vertices[v1 + 1] * v1cwinv) + 1.0f) * 0.5f * height;
            float v1cz = ((this.vertices[v1 + 2] * v1cwinv) + 1.0f) * 0.5f;
            float v1x = this.vertices[v1 + 4] * v1cwinv;
            float v1y = this.vertices[v1 + 5] * v1cwinv;
            float v1z = this.vertices[v1 + 6] * v1cwinv;
            float v1u = this.vertices[v1 + 7] * v1cwinv;
            float v1v = this.vertices[v1 + 8] * v1cwinv;
            float v1nx = this.vertices[v1 + 9] * v1cwinv;
            float v1ny = this.vertices[v1 + 10] * v1cwinv;
            float v1nz = this.vertices[v1 + 11] * v1cwinv;
            float v1r = this.vertices[v1 + 12] * v1cwinv;
            float v1g = this.vertices[v1 + 13] * v1cwinv;
            float v1b = this.vertices[v1 + 14] * v1cwinv;
            float v1a = this.vertices[v1 + 15] * v1cwinv;
            //

            //2
            float v2cwinv = 1f / this.vertices[v2 + 3];

            float v2cx = ((this.vertices[v2 + 0] * v2cwinv) + 1.0f) * 0.5f * width;
            float v2cy = ((this.vertices[v2 + 1] * v2cwinv) + 1.0f) * 0.5f * height;
            float v2cz = ((this.vertices[v2 + 2] * v2cwinv) + 1.0f) * 0.5f;
            float v2x = this.vertices[v2 + 4] * v2cwinv;
            float v2y = this.vertices[v2 + 5] * v2cwinv;
            float v2z = this.vertices[v2 + 6] * v2cwinv;
            float v2u = this.vertices[v2 + 7] * v2cwinv;
            float v2v = this.vertices[v2 + 8] * v2cwinv;
            float v2nx = this.vertices[v2 + 9] * v2cwinv;
            float v2ny = this.vertices[v2 + 10] * v2cwinv;
            float v2nz = this.vertices[v2 + 11] * v2cwinv;
            float v2r = this.vertices[v2 + 12] * v2cwinv;
            float v2g = this.vertices[v2 + 13] * v2cwinv;
            float v2b = this.vertices[v2 + 14] * v2cwinv;
            float v2a = this.vertices[v2 + 15] * v2cwinv;
            //

            float inverse = 1f / ((v1cy - v2cy) * (v0cx - v2cx) + (v2cx - v1cx) * (v0cy - v2cy));

            float maxX = Math.max(Math.max(v0cx, v1cx), v2cx);
            float maxY = Math.max(Math.max(v0cy, v1cy), v2cy);

            float minX = Math.min(Math.min(v0cx, v1cx), v2cx);
            float minY = Math.min(Math.min(v0cy, v1cy), v2cy);

            int maxXP = Math.round(maxX);
            int maxYP = Math.round(maxY);
            int minXP = Math.round(minX);
            int minYP = Math.round(minY);
            
            for (int y = minYP; y <= maxYP; y++) {
                for (int x = minXP; x <= maxXP; x++) {
                    if (x < 0 || x >= width || y < 0 || y >= height) {
                        continue;
                    }

                    float xPos = x + 0.5f;
                    float yPos = y + 0.5f;

                    float wv0 = ((v1cy - v2cy) * (xPos - v2cx) + (v2cx - v1cx) * (yPos - v2cy)) * inverse;
                    float wv1 = ((v2cy - v0cy) * (xPos - v2cx) + (v0cx - v2cx) * (yPos - v2cy)) * inverse;
                    float wv2 = 1 - wv0 - wv1;
                    if (wv0 < 0f || wv1 < 0f || wv2 < 0f) {
                        continue;
                    }

                    float invw = (wv0 * v0cwinv) + (wv1 * v1cwinv) + (wv2 * v2cwinv);
                    float w = 1f / invw;

                    float depth = (wv0 * v0cz) + (wv1 * v1cz) + (wv2 * v2cz);
                    float currentDepth = this.surface.getDepth(x, y);
                    if (depth > currentDepth) {
                        continue;
                    }
                    this.surface.setDepth(x, y, depth);
                    
                    if (this.depthOnly) {
                        continue;
                    }

                    float worldx = ((wv0 * v0x) + (wv1 * v1x) + (wv2 * v2x)) * w;
                    float worldy = ((wv0 * v0y) + (wv1 * v1y) + (wv2 * v2y)) * w;
                    float worldz = ((wv0 * v0z) + (wv1 * v1z) + (wv2 * v2z)) * w;

                    float u = ((wv0 * v0u) + (wv1 * v1u) + (wv2 * v2u)) * w;
                    float v = ((wv0 * v0v) + (wv1 * v1v) + (wv2 * v2v)) * w;

                    float nx = ((wv0 * v0nx) + (wv1 * v1nx) + (wv2 * v2nx)) * w;
                    float ny = ((wv0 * v0ny) + (wv1 * v1ny) + (wv2 * v2ny)) * w;
                    float nz = ((wv0 * v0nz) + (wv1 * v1nz) + (wv2 * v2nz)) * w;
                    float lengthinv = (float) (1.0 / Math.sqrt((nx * nx) + (ny * ny) + (nz * nz)));
                    nx *= lengthinv;
                    ny *= lengthinv;
                    nz *= lengthinv;

                    float cr = ((wv0 * v0r) + (wv1 * v1r) + (wv2 * v2r)) * w;
                    float cg = ((wv0 * v0g) + (wv1 * v1g) + (wv2 * v2g)) * w;
                    float cb = ((wv0 * v0b) + (wv1 * v1b) + (wv2 * v2b)) * w;
                    float ca = ((wv0 * v0a) + (wv1 * v1a) + (wv2 * v2a)) * w;

                    if (this.texture != null) {
                        if (this.bilinear) {
                            this.texture.sampleBilinear(u, v, textureColor);
                        } else {
                            this.texture.sampleNearest(u, v, textureColor);
                        }

                        cr *= textureColor[0];
                        cg *= textureColor[1];
                        cb *= textureColor[2];
                        ca *= textureColor[3];
                    }

                    float r = lightAmbient.x() * cr;
                    float g = lightAmbient.y() * cg;
                    float b = lightAmbient.z() * cb;
                    float a = ca;

                    float diffuse = Math.max((nx * -lightDirection.x()) + (ny * -lightDirection.y()) + (nz * -lightDirection.z()), 0f);

                    r += lightDiffuse.x() * diffuse * cr;
                    g += lightDiffuse.y() * diffuse * cg;
                    b += lightDiffuse.z() * diffuse * cb;

                    this.surface.getColor(x, y, color);

                    float outR = (r * a) + (color[0] * (1f - a));
                    float outG = (g * a) + (color[1] * (1f - a));
                    float outB = (b * a) + (color[2] * (1f - a));
                    
                    color[0] = outR;
                    color[1] = outG;
                    color[2] = outB;
                    this.surface.setColor(x, y, color);
                }
            }
        }
    }

}
