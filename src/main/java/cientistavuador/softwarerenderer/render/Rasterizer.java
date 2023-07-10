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
package cientistavuador.softwarerenderer.render;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

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
    private final boolean multithread;
    private final List<Light> lights;
    private final Vector4fc color;
    private final boolean lightingEnabled;

    private final class RasterizerVertex {

        public final float cwinv;

        public final float cx;
        public final float cy;
        public final float cz;
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

        public RasterizerVertex(int vertex, int width, int height) {
            this.cwinv = 1f / Rasterizer.this.vertices[vertex + 3];

            this.cx = ((Rasterizer.this.vertices[vertex + 0] * this.cwinv) + 1.0f) * 0.5f * width;
            this.cy = ((Rasterizer.this.vertices[vertex + 1] * this.cwinv) + 1.0f) * 0.5f * height;
            this.cz = ((Rasterizer.this.vertices[vertex + 2] * this.cwinv) + 1.0f) * 0.5f;
            this.x = Rasterizer.this.vertices[vertex + 4] * this.cwinv;
            this.y = Rasterizer.this.vertices[vertex + 5] * this.cwinv;
            this.z = Rasterizer.this.vertices[vertex + 6] * this.cwinv;
            this.u = Rasterizer.this.vertices[vertex + 7] * this.cwinv;
            this.v = Rasterizer.this.vertices[vertex + 8] * this.cwinv;
            this.nx = Rasterizer.this.vertices[vertex + 9] * this.cwinv;
            this.ny = Rasterizer.this.vertices[vertex + 10] * this.cwinv;
            this.nz = Rasterizer.this.vertices[vertex + 11] * this.cwinv;
            this.r = Rasterizer.this.vertices[vertex + 12] * this.cwinv;
            this.g = Rasterizer.this.vertices[vertex + 13] * this.cwinv;
            this.b = Rasterizer.this.vertices[vertex + 14] * this.cwinv;
            this.a = Rasterizer.this.vertices[vertex + 15] * this.cwinv;
        }
    }

    public Rasterizer(Surface surface, Texture texture, float[] vertices, Vector3fc lightDirection, Vector3fc lightDiffuse, Vector3fc lightAmbient, boolean depthOnly, boolean bilinear, boolean multithread, List<Light> lights, Vector4fc color, boolean lightingEnabled) {
        this.surface = surface;
        this.texture = texture;
        this.vertices = vertices;
        this.lightDirection = lightDirection;
        this.lightDiffuse = lightDiffuse;
        this.lightAmbient = lightAmbient;
        this.depthOnly = depthOnly;
        this.bilinear = bilinear;
        this.multithread = multithread;
        this.lights = lights;
        this.color = color;
        this.lightingEnabled = lightingEnabled;
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

    public List<Light> getLights() {
        return lights;
    }

    public Vector4fc getColor() {
        return color;
    }

    public void render() {
        int width = this.surface.getWidth();
        int height = this.surface.getHeight();
        int numberOfTriangles = this.vertices.length / (VertexProcessor.PROCESSED_VERTEX_SIZE * 3);
        for (int i = 0; i < numberOfTriangles; i++) {
            int v0i = i * (VertexProcessor.PROCESSED_VERTEX_SIZE * 3);
            int v1i = v0i + VertexProcessor.PROCESSED_VERTEX_SIZE;
            int v2i = v1i + VertexProcessor.PROCESSED_VERTEX_SIZE;
            RasterizerVertex v0 = new RasterizerVertex(v0i, width, height);
            RasterizerVertex v1 = new RasterizerVertex(v1i, width, height);
            RasterizerVertex v2 = new RasterizerVertex(v2i, width, height);

            float inverse = 1f / ((v1.cy - v2.cy) * (v0.cx - v2.cx) + (v2.cx - v1.cx) * (v0.cy - v2.cy));

            float maxX = Math.max(Math.max(v0.cx, v1.cx), v2.cx);
            float maxY = Math.max(Math.max(v0.cy, v1.cy), v2.cy);

            float minX = Math.min(Math.min(v0.cx, v1.cx), v2.cx);
            float minY = Math.min(Math.min(v0.cy, v1.cy), v2.cy);

            int maxXP = clamp(Math.round(maxX), 0, width);
            int maxYP = clamp(Math.round(maxY), 0, height);
            int minXP = clamp(Math.round(minX), 0, width - 1);
            int minYP = clamp(Math.round(minY), 0, height - 1);

            boolean multithreadActivated = (maxXP - minXP) >= 32 && this.multithread;

            Future<?>[] tasks = new Future<?>[maxYP - minYP];
            for (int y = minYP; y < maxYP; y++) {
                if (multithreadActivated) {
                    int finalY = y;
                    tasks[y - minYP] = CompletableFuture.runAsync(() -> {
                        renderLine(inverse, finalY, minXP, maxXP, v0, v1, v2);
                    });
                } else {
                    renderLine(inverse, y, minXP, maxXP, v0, v1, v2);
                }
            }
            if (multithreadActivated) {
                try {
                    for (Future<?> task : tasks) {
                        task.get();
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private int clamp(int v, int min, int max) {
        return Math.max(Math.min(v, max), min);
    }

    private void renderLine(float inverse, int y, int minX, int maxX, RasterizerVertex v0, RasterizerVertex v1, RasterizerVertex v2) {
        float[] surfaceDepth = new float[maxX - minX];
        this.surface.getDepth(minX, y, surfaceDepth, 0, surfaceDepth.length);
        float[] surfaceColor = new float[(maxX - minX) * 3];
        this.surface.getColor(minX, y, surfaceColor, 0, surfaceColor.length);

        float[] textureColor = new float[4];
        float[] diffuseAmbientFactors = new float[2];
        for (int x = minX; x < maxX; x++) {
            int pixelIndex = x - minX;

            float xPos = x + 0.5f;
            float yPos = y + 0.5f;

            float wv0 = ((v1.cy - v2.cy) * (xPos - v2.cx) + (v2.cx - v1.cx) * (yPos - v2.cy)) * inverse;
            float wv1 = ((v2.cy - v0.cy) * (xPos - v2.cx) + (v0.cx - v2.cx) * (yPos - v2.cy)) * inverse;
            float wv2 = 1 - wv0 - wv1;
            if (wv0 < 0f || wv1 < 0f || wv2 < 0f) {
                continue;
            }

            float invw = (wv0 * v0.cwinv) + (wv1 * v1.cwinv) + (wv2 * v2.cwinv);
            float w = 1f / invw;

            float depth = (wv0 * v0.cz) + (wv1 * v1.cz) + (wv2 * v2.cz);
            float currentDepth = surfaceDepth[pixelIndex];
            if (depth > currentDepth) {
                surfaceDepth[pixelIndex] = currentDepth;
                continue;
            }
            surfaceDepth[pixelIndex] = depth;

            if (this.depthOnly) {
                continue;
            }

            float u = ((wv0 * v0.u) + (wv1 * v1.u) + (wv2 * v2.u)) * w;
            float v = ((wv0 * v0.v) + (wv1 * v1.v) + (wv2 * v2.v)) * w;

            float cr = this.color.x();
            float cg = this.color.y();
            float cb = this.color.z();
            float ca = this.color.w();

            cr *= ((wv0 * v0.r) + (wv1 * v1.r) + (wv2 * v2.r)) * w;
            cg *= ((wv0 * v0.g) + (wv1 * v1.g) + (wv2 * v2.g)) * w;
            cb *= ((wv0 * v0.b) + (wv1 * v1.b) + (wv2 * v2.b)) * w;
            ca *= ((wv0 * v0.a) + (wv1 * v1.a) + (wv2 * v2.a)) * w;

            if (this.texture != null) {
                if (this.bilinear) {
                    this.texture.sampleBilinear(u, v, textureColor, 0);
                } else {
                    this.texture.sampleNearest(u, v, textureColor, 0);
                }

                cr *= textureColor[0];
                cg *= textureColor[1];
                cb *= textureColor[2];
                ca *= textureColor[3];
            }

            if (this.lightingEnabled) {
                float worldx = ((wv0 * v0.x) + (wv1 * v1.x) + (wv2 * v2.x)) * w;
                float worldy = ((wv0 * v0.y) + (wv1 * v1.y) + (wv2 * v2.y)) * w;
                float worldz = ((wv0 * v0.z) + (wv1 * v1.z) + (wv2 * v2.z)) * w;

                float nx = ((wv0 * v0.nx) + (wv1 * v1.nx) + (wv2 * v2.nx)) * w;
                float ny = ((wv0 * v0.ny) + (wv1 * v1.ny) + (wv2 * v2.ny)) * w;
                float nz = ((wv0 * v0.nz) + (wv1 * v1.nz) + (wv2 * v2.nz)) * w;
                float lengthinv = (float) (1.0 / Math.sqrt((nx * nx) + (ny * ny) + (nz * nz)));
                nx *= lengthinv;
                ny *= lengthinv;
                nz *= lengthinv;
                
                float r = lightAmbient.x() * cr;
                float g = lightAmbient.y() * cg;
                float b = lightAmbient.z() * cb;
                
                float diffuse = Math.max((nx * -lightDirection.x()) + (ny * -lightDirection.y()) + (nz * -lightDirection.z()), 0f);

                r += lightDiffuse.x() * diffuse * cr;
                g += lightDiffuse.y() * diffuse * cg;
                b += lightDiffuse.z() * diffuse * cb;

                for (Light light : this.lights) {
                    if (light != null) {
                        light.calculateDiffuseAmbientFactors(worldx, worldy, worldz, nx, ny, nz, diffuseAmbientFactors);

                        r += diffuseAmbientFactors[0] * light.getDiffuseColor().x() * cr;
                        g += diffuseAmbientFactors[0] * light.getDiffuseColor().y() * cg;
                        b += diffuseAmbientFactors[0] * light.getDiffuseColor().z() * cb;

                        r += diffuseAmbientFactors[1] * light.getAmbientColor().x() * cr;
                        g += diffuseAmbientFactors[1] * light.getAmbientColor().y() * cg;
                        b += diffuseAmbientFactors[1] * light.getAmbientColor().z() * cb;
                    }
                }
                
                cr = r;
                cg = g;
                cb = b;
            }

            float outR = (cr * ca) + (surfaceColor[(pixelIndex * 3) + 0] * (1f - ca));
            float outG = (cg * ca) + (surfaceColor[(pixelIndex * 3) + 1] * (1f - ca));
            float outB = (cb * ca) + (surfaceColor[(pixelIndex * 3) + 2] * (1f - ca));

            surfaceColor[(pixelIndex * 3) + 0] = outR;
            surfaceColor[(pixelIndex * 3) + 1] = outG;
            surfaceColor[(pixelIndex * 3) + 2] = outB;
        }

        this.surface.setDepth(minX, y, surfaceDepth, 0, surfaceDepth.length);
        this.surface.setColor(minX, y, surfaceColor, 0, surfaceColor.length);
    }

}
