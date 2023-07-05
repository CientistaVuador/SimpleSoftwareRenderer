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

import cientistavuador.simplesoftwarerenderer.util.Pixels;
import java.awt.image.BufferedImage;

/**
 *
 * @author Cien
 */
public class AWTInterop {
    
    public static Texture toTexture(BufferedImage image) {
        final int width = image.getWidth();
        final int height = image.getHeight();
        final int[] bufferedData = image.getRGB(0, 0, width, height, null, 0, width);
        final float[] pixelData = new float[width * height * 4];
        
        for (int i = 0; i < width*height; i++) {
            int x = i % width;
            int y = i / width;
            
            int pixel = bufferedData[x + (((height-1) - y) * width)];
            
            pixelData[((x + (y * width)) * 4) + 0] = Pixels.decodeNormalized(pixel, 1);
            pixelData[((x + (y * width)) * 4) + 1] = Pixels.decodeNormalized(pixel, 2);
            pixelData[((x + (y * width)) * 4) + 2] = Pixels.decodeNormalized(pixel, 3);
            pixelData[((x + (y * width)) * 4) + 3] = Pixels.decodeNormalized(pixel, 0);
        }
        
        return new Texture() {
            @Override
            public int width() {
                return width;
            }

            @Override
            public int height() {
                return height;
            }

            @Override
            public void fetch(int x, int y, float[] result) {
                System.arraycopy(pixelData, (x + (y * width)) * 4, result, 0, 4);
            }
        };
    }
    
    public static BufferedImage fromTexture(Texture t) {
        if (t == null) {
            throw new NullPointerException("Texture is null.");
        }
        
        int width = t.width();
        int height = t.height();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        float[] cache = new float[4];
        int[] data = new int[width*height];
        
        for (int i = 0; i < width*height; i++) {
            int x = i % width;
            int y = i / width;
            
            t.fetch(x, (height - y) - 1, cache);
            data[i] = Pixels.encodeNormalized(
                    cache[3],
                    cache[0],
                    cache[1],
                    cache[2]
            );
        }
        
        image.setRGB(0, 0, width, height, data, 0, width);
        
        return image;
    }
    
    private AWTInterop() {
        
    }
}
