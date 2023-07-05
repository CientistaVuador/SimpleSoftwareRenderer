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

/**
 *
 * @author Cien
 */
public interface Texture {
    public int width();
    public int height();
    public default void sampleNearest(float x, float y, float[] result) {
        int width = width();
        int height = height();
        int pX = (Math.round(Math.abs(x) * width) % width);
        int pY = (Math.round(Math.abs(y) * height) % height);
        fetch(pX, pY, result);
    }
    public default void sampleBilinear(float x, float y, float[] result) {
        int width = width();
        int height = height();
        
        float pX = Math.abs(x) * width;
        float pY = Math.abs(y) * height;
        
        int bottomLeftX = (int) Math.floor(pX);
        int bottomLeftY = (int) Math.floor(pY);
        
        float weightX = pX - bottomLeftX;
        float weightY = pY - bottomLeftY;
        
        fetch(bottomLeftX % width, bottomLeftY % height, result);
        float bottomLeftR = result[0];
        float bottomLeftG = result[1];
        float bottomLeftB = result[2];
        float bottomLeftA = result[3];
        
        int bottomRightX = (int) Math.ceil(pX);
        int bottomRightY = (int) Math.floor(pY);
        
        fetch(bottomRightX % width, bottomRightY % height, result);
        float bottomRightR = result[0];
        float bottomRightG = result[1];
        float bottomRightB = result[2];
        float bottomRightA = result[3];
        
        int topLeftX = (int) Math.floor(pX);
        int topLeftY = (int) Math.ceil(pY);
        
        fetch(topLeftX % width, topLeftY % height, result);
        float topLeftR = result[0];
        float topLeftG = result[1];
        float topLeftB = result[2];
        float topLeftA = result[3];
        
        int topRightX = (int) Math.ceil(pX);
        int topRightY = (int) Math.ceil(pY);
        
        fetch(topRightX % width, topRightY % height, result);
        float topRightR = result[0];
        float topRightG = result[1];
        float topRightB = result[2];
        float topRightA = result[3];
        
        result[0] = (bottomLeftR * (1f - weightX) * (1f - weightY)) + (bottomRightR * weightX * (1f - weightY)) + (topLeftR * (1f - weightX) * weightY) + (topRightR * weightX * weightY);
        result[1] = (bottomLeftG * (1f - weightX) * (1f - weightY)) + (bottomRightG * weightX * (1f - weightY)) + (topLeftG * (1f - weightX) * weightY) + (topRightG * weightX * weightY);
        result[2] = (bottomLeftB * (1f - weightX) * (1f - weightY)) + (bottomRightB * weightX * (1f - weightY)) + (topLeftB * (1f - weightX) * weightY) + (topRightB * weightX * weightY);
        result[3] = (bottomLeftA * (1f - weightX) * (1f - weightY)) + (bottomRightA * weightX * (1f - weightY)) + (topLeftA * (1f - weightX) * weightY) + (topRightA * weightX * weightY);
    }
    public void fetch(int x, int y, float[] result);
}
