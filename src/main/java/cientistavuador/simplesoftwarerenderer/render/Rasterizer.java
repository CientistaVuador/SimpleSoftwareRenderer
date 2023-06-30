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

import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 *
 * @author Cien
 */
public class Rasterizer {

    private final Surface surface;
    private final Texture texture;
    private final float[] vertices;

    public Rasterizer(Surface surface, Texture texture, float[] vertices) {
        this.surface = surface;
        this.texture = texture;
        this.vertices = vertices;
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

    public void render() {
        Vector3f color = new Vector3f();
        Vector4f textureColor = new Vector4f();
        float width = this.surface.getWidth();
        float height = this.surface.getHeight();
        for (int i = 0; i < (this.vertices.length / (VerticesProcessor.PROCESSED_VERTEX_SIZE * 3)); i++) {
            int v0 = i * (VerticesProcessor.PROCESSED_VERTEX_SIZE * 3);
            int v1 = v0 + VerticesProcessor.PROCESSED_VERTEX_SIZE;
            int v2 = v1 + VerticesProcessor.PROCESSED_VERTEX_SIZE;

            //0
            float v0cx = this.vertices[v0 + 0] * width;
            float v0cy = this.vertices[v0 + 1] * height;
            float v0cz = this.vertices[v0 + 2];
            float v0invw = this.vertices[v0 + 3];
            float v0x = this.vertices[v0 + 4];
            float v0y = this.vertices[v0 + 5];
            float v0z = this.vertices[v0 + 6];
            float v0u = this.vertices[v0 + 7];
            float v0v = this.vertices[v0 + 8];
            float v0nx = this.vertices[v0 + 9];
            float v0ny = this.vertices[v0 + 10];
            float v0nz = this.vertices[v0 + 11];
            float v0r = this.vertices[v0 + 12];
            float v0g = this.vertices[v0 + 13];
            float v0b = this.vertices[v0 + 14];
            float v0a = this.vertices[v0 + 15];
            //

            //1
            float v1cx = this.vertices[v1 + 0] * width;
            float v1cy = this.vertices[v1 + 1] * height;
            float v1cz = this.vertices[v1 + 2];
            float v1invw = this.vertices[v1 + 3];
            float v1x = this.vertices[v1 + 4];
            float v1y = this.vertices[v1 + 5];
            float v1z = this.vertices[v1 + 6];
            float v1u = this.vertices[v1 + 7];
            float v1v = this.vertices[v1 + 8];
            float v1nx = this.vertices[v1 + 9];
            float v1ny = this.vertices[v1 + 10];
            float v1nz = this.vertices[v1 + 11];
            float v1r = this.vertices[v1 + 12];
            float v1g = this.vertices[v1 + 13];
            float v1b = this.vertices[v1 + 14];
            float v1a = this.vertices[v1 + 15];
            //

            //2
            float v2cx = this.vertices[v2 + 0] * width;
            float v2cy = this.vertices[v2 + 1] * height;
            float v2cz = this.vertices[v2 + 2];
            float v2invw = this.vertices[v2 + 3];
            float v2x = this.vertices[v2 + 4];
            float v2y = this.vertices[v2 + 5];
            float v2z = this.vertices[v2 + 6];
            float v2u = this.vertices[v2 + 7];
            float v2v = this.vertices[v2 + 8];
            float v2nx = this.vertices[v2 + 9];
            float v2ny = this.vertices[v2 + 10];
            float v2nz = this.vertices[v2 + 11];
            float v2r = this.vertices[v2 + 12];
            float v2g = this.vertices[v2 + 13];
            float v2b = this.vertices[v2 + 14];
            float v2a = this.vertices[v2 + 15];
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
            
            for (int x = minXP; x <= maxXP; x++) {
                for (int y = minYP; y <= maxYP; y++) {
                    float xPos = x + 0.5f;
                    float yPos = y + 0.5f;
                    
                    float wv0 = ((v1cy - v2cy) * (xPos - v2cx) + (v2cx - v1cx) * (yPos - v2cy)) * inverse;
                    float wv1 = ((v2cy - v0cy) * (xPos - v2cx) + (v0cx - v2cx) * (yPos - v2cy)) * inverse;
                    float wv2 = 1 - wv0 - wv1;
                    if (wv0 < 0f || wv1 < 0f || wv2 < 0f) {
                        continue;
                    }

                    float invw = (wv0 * v0invw) + (wv1 * v1invw) + (wv2 * v2invw);
                    float w = 1f / invw;

                    float r = (wv0 * v0r) + (wv1 * v1r) + (wv2 * v2r);
                    float g = (wv0 * v0g) + (wv1 * v1g) + (wv2 * v2g);
                    float b = (wv0 * v0b) + (wv1 * v1b) + (wv2 * v2b);
                    float a = (wv0 * v0a) + (wv1 * v1a) + (wv2 * v2a);
                    r *= w;
                    g *= w;
                    b *= w;
                    a *= w;

                    float u = (wv0 * v0u) + (wv1 * v1u) + (wv2 * v2u);
                    float v = (wv0 * v0v) + (wv1 * v1v) + (wv2 * v2v);
                    u *= w;
                    v *= w;

                    if (this.texture != null) {
                        this.texture.sample(u, v, textureColor);

                        r *= textureColor.x();
                        g *= textureColor.y();
                        b *= textureColor.z();
                        a *= textureColor.w();
                    }

                    this.surface.getColor(x, y, color);

                    float outR = (r * a) + (color.x() * (1f - a));
                    float outG = (g * a) + (color.y() * (1f - a));
                    float outB = (b * a) + (color.z() * (1f - a));

                    this.surface.setColor(x, y, color.set(outR, outG, outB));
                }
            }

        }
    }

}
