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

import java.awt.image.BufferedImage;
import org.joml.Matrix4f;
import org.joml.Vector3f;

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

    private Surface frontSurface;
    private Surface backSurface;

    //state
    private float clearDepth = 1f;
    private final Vector3f clearColor = new Vector3f(0.2f, 0.4f, 0.6f);
    private VertexBuilder builder = null;
    private float[] vertices = null;
    private Matrix4f projectionView = null;
    private Matrix4f model = null;
    private boolean depthOnly = false;
    private boolean bilinearFiltering = false;

    private final Vector3f lightDirection = new Vector3f(-1f, -1f, -1f).normalize();
    private final Vector3f lightDiffuse = new Vector3f(0.8f, 0.8f, 0.8f);
    private final Vector3f lightAmbient = new Vector3f(0.2f, 0.2f, 0.2f);
    private Texture texture = null;

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

    public Matrix4f getProjectionView() {
        return projectionView;
    }

    public void setProjectionView(Matrix4f projectionView) {
        this.projectionView = projectionView;
    }

    public Matrix4f getModel() {
        return model;
    }

    public void setModel(Matrix4f model) {
        this.model = model;
    }

    //rasterizer
    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Texture getTexture() {
        return texture;
    }

    public Vector3f getLightDirection() {
        return lightDirection;
    }

    public Vector3f getLightDiffuse() {
        return lightDiffuse;
    }

    public Vector3f getLightAmbient() {
        return lightAmbient;
    }

    public boolean isDepthOnly() {
        return depthOnly;
    }

    public void setDepthOnly(boolean depthOnly) {
        this.depthOnly = depthOnly;
    }

    public boolean isBilinearFiltering() {
        return bilinearFiltering;
    }

    public void setBilinearFiltering(boolean bilinearFiltering) {
        this.bilinearFiltering = bilinearFiltering;
    }

    //render
    public int render() {
        if (this.vertices == null || this.vertices.length == 0) {
            return 0;
        }
        VertexProcessor processor = new VertexProcessor(this.vertices, this.projectionView, this.model);
        float[] processed = processor.process();

        Rasterizer rasterizer = new Rasterizer(
                this.frontSurface,
                this.texture,
                processed,
                this.lightDirection,
                this.lightDiffuse,
                this.lightAmbient,
                this.depthOnly,
                this.bilinearFiltering
        );
        rasterizer.render();

        return (processed.length / VertexProcessor.PROCESSED_VERTEX_SIZE);
    }

}

