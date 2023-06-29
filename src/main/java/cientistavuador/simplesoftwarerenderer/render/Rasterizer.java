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
        for (int i = 0; i < (this.vertices.length / (VerticesStream.VERTEX_SIZE * 3)); i++) {
            int v0 = i * (VerticesStream.VERTEX_SIZE * 3);
            int v1 = v0 + VerticesStream.VERTEX_SIZE;
            int v2 = v1 + VerticesStream.VERTEX_SIZE;
            
            //0
            float v0x = this.vertices[v0 + 0];
            float v0y = this.vertices[v0 + 1];
            float v0z = this.vertices[v0 + 2];
            float v0w = this.vertices[v0 + 3];
            
            v0x /= v0w;
            v0y /= v0w;
            v0z /= v0w;
            
            float v0invW = 1f / v0w;
            
            float v0u = this.vertices[v0 + 7];
            float v0v = this.vertices[v0 + 8];
            v0u /= v0w;
            v0v /= v0w;
            
            float v0r = this.vertices[v0 + 12];
            float v0g = this.vertices[v0 + 13];
            float v0b = this.vertices[v0 + 14];
            float v0a = this.vertices[v0 + 15];
            v0r /= v0w;
            v0g /= v0w;
            v0b /= v0w;
            v0a /= v0w;
            
            //1
            float v1x = this.vertices[v1 + 0];
            float v1y = this.vertices[v1 + 1];
            float v1z = this.vertices[v1 + 2];
            float v1w = this.vertices[v1 + 3];
            
            v1x /= v1w;
            v1y /= v1w;
            v1z /= v1w;
            
            float v1invW = 1f / v1w;
            
            float v1u = this.vertices[v1 + 7];
            float v1v = this.vertices[v1 + 8];
            v1u /= v1w;
            v1v /= v1w;
            
            float v1r = this.vertices[v1 + 12];
            float v1g = this.vertices[v1 + 13];
            float v1b = this.vertices[v1 + 14];
            float v1a = this.vertices[v1 + 15];
            v1r /= v1w;
            v1g /= v1w;
            v1b /= v1w;
            v1a /= v1w;
            
            //2
            float v2x = this.vertices[v2 + 0];
            float v2y = this.vertices[v2 + 1];
            float v2z = this.vertices[v2 + 2];
            float v2w = this.vertices[v2 + 3];
            
            v2x /= v2w;
            v2y /= v2w;
            v2z /= v2w;
            
            float v2invW = 1f / v2w;
            
            float v2u = this.vertices[v2 + 7];
            float v2v = this.vertices[v2 + 8];
            v2u /= v2w;
            v2v /= v2w;
            
            float v2r = this.vertices[v2 + 12];
            float v2g = this.vertices[v2 + 13];
            float v2b = this.vertices[v2 + 14];
            float v2a = this.vertices[v2 + 15];
            v2r /= v2w;
            v2g /= v2w;
            v2b /= v2w;
            v2a /= v2w;
            
            float maxX = Math.max(Math.max(v0x, v1x), v2x);
            float maxY = Math.max(Math.max(v0y, v1y), v2y);
            
            float minX = Math.min(Math.min(v0x, v1x), v2x);
            float minY = Math.min(Math.min(v0y, v1y), v2y);
            
            int maxXP = Math.round(((maxX + 1f) * 0.5f) * width);
            int maxYP = Math.round(((maxY + 1f) * 0.5f) * height);
            int minXP = Math.round(((minX + 1f) * 0.5f) * width);
            int minYP = Math.round(((minY + 1f) * 0.5f) * height);
            
            for (int x = minXP; x <= maxXP; x++) {
                for (int y = minYP; y <= maxYP; y++) {
                    float xPos = (((x + 0.5f) / width) * 2f) - 1f;
                    float yPos = (((y + 0.5f) / height) * 2f) - 1f;
                    if (xPos < -1f || xPos > 1f || yPos < -1f || yPos > 1f) {
                        continue;
                    }
                    
                    float inv = 1f / ((v1y - v2y) * (v0x - v2x) + (v2x - v1x) * (v0y - v2y));
                    float wv0 = ((v1y - v2y) * (xPos - v2x) + (v2x - v1x) * (yPos - v2y)) * inv;
                    float wv1 = ((v2y - v0y) * (xPos - v2x) + (v0x - v2x) * (yPos - v2y)) * inv;
                    float wv2 = 1 - wv0 - wv1;
                    if (wv0 < 0f || wv1 < 0f || wv2 < 0f) {
                        continue;
                    }
                    
                    float invW = (wv0 * v0invW) + (wv1 * v1invW) + (wv2 * v2invW);
                    
                    float r = (wv0 * v0r) + (wv1 * v1r) + (wv2 * v2r);
                    float g = (wv0 * v0g) + (wv1 * v1g) + (wv2 * v2g);
                    float b = (wv0 * v0b) + (wv1 * v1b) + (wv2 * v2b);
                    float a = (wv0 * v0a) + (wv1 * v1a) + (wv2 * v2a);
                    r /= invW;
                    g /= invW;
                    b /= invW;
                    a /= invW;
                    
                    float u = (wv0 * v0u) + (wv1 * v1u) + (wv2 * v2u);
                    float v = (wv0 * v0v) + (wv1 * v1v) + (wv2 * v2v);
                    u /= invW;
                    v /= invW;
                    
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
