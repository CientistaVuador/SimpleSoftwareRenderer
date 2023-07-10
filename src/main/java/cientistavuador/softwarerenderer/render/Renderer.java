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

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 *
 * @author Cien
 */
public class Renderer {

    public static Renderer create(int width, int height) {
        return new Renderer(new Surface(width, height), new Surface(width, height));
    }

    public static Renderer create() {
        return create(Surface.DEFAULT_WIDTH, Surface.DEFAULT_HEIGHT);
    }

    //surface
    private Surface frontSurface;
    private Surface backSurface;

    //vertex builder
    private VertexBuilder builder = null;
    
    //surface state
    private final Vector3f clearColor = new Vector3f(0.2f, 0.4f, 0.6f);
    private float clearDepth = 1f;
    
    //rasterizer/processor state
    private boolean depthOnlyEnabled = false;
    private boolean bilinearFilteringEnabled = false;
    private boolean multithreadEnabled = true;
    private boolean billboardingEnabled = false;
    private boolean lightingEnabled = true;
    
    //sun state
    private final Vector3f sunDirection = new Vector3f(-1f, -1f, -1f).normalize();
    private final Vector3f sunDiffuse = new Vector3f(0.8f, 0.75f, 0.70f);
    private final Vector3f sunAmbient = new Vector3f(0.3f, 0.3f, 0.3f);
    
    //lights state
    private final List<Light> lights = new ArrayList<>();
    
    //camera state
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f view = new Matrix4f();
    private final Vector3f cameraPosition = new Vector3f();
    
    //object state
    private float[] vertices = null;
    private final Matrix4f model = new Matrix4f();
    private Texture texture = null;
    private final Vector4f color = new Vector4f(1f, 1f, 1f, 1f);
    
    private Renderer(Surface frontSurface, Surface backSurface) {
        this.frontSurface = frontSurface;
        this.backSurface = backSurface;
    }

    //awt interop
    public Texture imageToTexture(BufferedImage img) {
        return AWTInterop.toTexture(img);
    }

    public BufferedImage textureToImage(Texture tex) {
        return AWTInterop.fromTexture(tex);
    }

    //surface
    public Surface getSurface() {
        return this.frontSurface;
    }

    public float getClearDepth() {
        return clearDepth;
    }

    public void setClearDepth(float clearDepth) {
        this.clearDepth = clearDepth;
    }

    public Vector3f getClearColor() {
        return clearColor;
    }

    public void clearBuffers() {
        this.frontSurface.clearDepth(this.clearDepth);
        this.frontSurface.clearColor(this.clearColor.x(), this.clearColor.y(), this.clearColor.z());
    }

    public void resize(int width, int height) {
        this.frontSurface = new Surface(width, height);
        this.backSurface = new Surface(width, height);
    }

    public int getWidth() {
        return this.frontSurface.getWidth();
    }

    public int getHeight() {
        return this.frontSurface.getHeight();
    }

    public Texture colorBuffer() {
        return this.frontSurface.getColorBufferTexture();
    }

    public Texture depthBuffer() {
        return this.frontSurface.getDepthBufferTexture();
    }

    public BufferedImage colorBufferToImage() {
        return this.textureToImage(this.colorBuffer());
    }

    public BufferedImage depthBufferToImage() {
        return this.textureToImage(this.depthBuffer());
    }

    public void flipSurfaces() {
        Surface front = this.frontSurface;
        Surface back = this.backSurface;
        this.frontSurface = back;
        this.backSurface = front;
    }

    //vertex builder
    public void beginVertices() {
        this.builder = new VertexBuilder();
    }

    public int position(float x, float y, float z) {
        return this.builder.position(x, y, z);
    }

    public int texture(float u, float v) {
        return this.builder.texture(u, v);
    }

    public int normal(float nX, float nY, float nZ) {
        return this.builder.normal(nX, nY, nZ);
    }

    public int color(float r, float g, float b, float a) {
        return this.builder.color(r, g, b, a);
    }

    public void vertex(int positionIndex, int textureIndex, int normalIndex, int colorIndex) {
        this.builder.vertex(positionIndex, textureIndex, normalIndex, colorIndex);
    }

    public float[] finishVertices() {
        VertexBuilder e = this.builder;
        this.builder = null;
        return e.vertices();
    }

    public void finishVerticesAndSet() {
        this.setVertices(this.finishVertices());
    }

    //vertex processor state
    public float[] getVertices() {
        return vertices;
    }

    public void setVertices(float[] vertices) {
        this.vertices = vertices;
    }

    public Matrix4f getProjection() {
        return projection;
    }
    
    public Matrix4f getView() {
        return view;
    }

    public Vector3f getCameraPosition() {
        return cameraPosition;
    }
    
    public Matrix4f getModel() {
        return model;
    }
    
    public boolean isBillboardingEnabled() {
        return billboardingEnabled;
    }

    public void setBillboardingEnabled(boolean billboardingEnabled) {
        this.billboardingEnabled = billboardingEnabled;
    }
    
    //rasterizer
    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    public Vector3f getSunDirection() {
        return sunDirection;
    }

    public Vector3f getSunDiffuse() {
        return sunDiffuse;
    }

    public Vector3f getSunAmbient() {
        return sunAmbient;
    }

    public boolean isDepthOnlyEnabled() {
        return depthOnlyEnabled;
    }

    public void setDepthOnlyEnabled(boolean depthOnlyEnabled) {
        this.depthOnlyEnabled = depthOnlyEnabled;
    }

    public boolean isBilinearFilteringEnabled() {
        return bilinearFilteringEnabled;
    }

    public void setBilinearFilteringEnabled(boolean bilinearFilteringEnabled) {
        this.bilinearFilteringEnabled = bilinearFilteringEnabled;
    }

    public boolean isMultithreadEnabled() {
        return multithreadEnabled;
    }

    public void setMultithreadEnabled(boolean multithreadEnabled) {
        this.multithreadEnabled = multithreadEnabled;
    }

    public List<Light> getLights() {
        return lights;
    }

    public Vector4f getColor() {
        return color;
    }

    public boolean isLightingEnabled() {
        return lightingEnabled;
    }

    public void setLightingEnabled(boolean lightingEnabled) {
        this.lightingEnabled = lightingEnabled;
    }

    //render
    public int render() {
        if (this.vertices == null || this.vertices.length == 0) {
            return 0;
        }
        VertexProcessor processor = new VertexProcessor(
                this.vertices,
                this.projection,
                this.view,
                this.cameraPosition,
                this.model,
                this.billboardingEnabled
        );
        float[] processed = processor.process();
        
        Rasterizer rasterizer = new Rasterizer(
                this.frontSurface,
                this.texture,
                processed,
                this.sunDirection,
                this.sunDiffuse,
                this.sunAmbient,
                this.depthOnlyEnabled,
                this.bilinearFilteringEnabled,
                this.multithreadEnabled,
                this.lights,
                this.color,
                this.lightingEnabled
        );
        rasterizer.render();

        return (processed.length / VertexProcessor.PROCESSED_VERTEX_SIZE);
    }

}

