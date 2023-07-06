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
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 *
 * @author Cien
 */
public class Surface {

    public static final int DEFAULT_WIDTH = 200;
    public static final int DEFAULT_HEIGHT = 150;

    private final int width;
    private final int height;

    private final float[] colorBuffer;
    private final float[] depthBuffer;

    private final Texture colorBufferTexture;
    private final Texture depthBufferTexture;

    public Surface(int width, int height) {
        this.width = width;
        this.height = height;
        this.colorBuffer = new float[width * height * 3];
        this.depthBuffer = new float[width * height];
        int[] atomicArray = new int[width * height];
        Arrays.fill(atomicArray, -1);
        this.colorBufferTexture = new Texture() {
            @Override
            public int width() {
                return Surface.this.width;
            }

            @Override
            public int height() {
                return Surface.this.height;
            }

            @Override
            public void fetch(int x, int y, float[] result) {
                result[0] = Surface.this.colorBuffer[((x + (y * width())) * 3) + 0];
                result[1] = Surface.this.colorBuffer[((x + (y * width())) * 3) + 1];
                result[2] = Surface.this.colorBuffer[((x + (y * width())) * 3) + 2];
                result[3] = 1f;
            }
        };
        this.depthBufferTexture = new Texture() {
            @Override
            public int width() {
                return Surface.this.width;
            }

            @Override
            public int height() {
                return Surface.this.height;
            }

            @Override
            public void fetch(int x, int y, float[] result) {
                float depth = Surface.this.depthBuffer[x + (y * width())];
                result[0] = depth;
                result[1] = depth;
                result[2] = depth;
                result[3] = 1f;
            }
        };
    }

    public Surface() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Texture getColorBufferTexture() {
        return colorBufferTexture;
    }

    public Texture getDepthBufferTexture() {
        return depthBufferTexture;
    }

    public void setColor(int x, int y, float[] rgb) {
        this.colorBuffer[((x + (y * getWidth())) * 3) + 0] = rgb[0];
        this.colorBuffer[((x + (y * getWidth())) * 3) + 1] = rgb[1];
        this.colorBuffer[((x + (y * getWidth())) * 3) + 2] = rgb[2];
    }

    public void setDepth(int x, int y, float depth) {
        this.depthBuffer[x + (y * getWidth())] = depth;
    }

    public void getColor(int x, int y, float[] rgb) {
        rgb[0] = this.colorBuffer[((x + (y * getWidth())) * 3) + 0];
        rgb[1] = this.colorBuffer[((x + (y * getWidth())) * 3) + 1];
        rgb[2] = this.colorBuffer[((x + (y * getWidth())) * 3) + 2];
    }

    public float getDepth(int x, int y) {
        return this.depthBuffer[x + (y * getWidth())];
    }
    
    public void clearColor(float r, float g, float b) {
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
                this.colorBuffer[((x + (y * getWidth())) * 3) + 0] = r;
                this.colorBuffer[((x + (y * getWidth())) * 3) + 1] = g;
                this.colorBuffer[((x + (y * getWidth())) * 3) + 2] = b;
            }
        }
    }

    public void clearDepth(float depth) {
        Arrays.fill(this.depthBuffer, depth);
    }
}
