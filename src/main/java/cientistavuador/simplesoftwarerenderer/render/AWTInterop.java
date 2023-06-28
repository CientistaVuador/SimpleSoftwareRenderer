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
import org.joml.Vector4f;

/**
 *
 * @author Cien
 */
public class AWTInterop {
    
    public static class BufferedTexture implements Texture {

        private final BufferedImage image;

        public BufferedTexture(BufferedImage image) {
            this.image = image;
        }

        public BufferedImage getImage() {
            return image;
        }
        
        @Override
        public int width() {
            return image.getWidth();
        }

        @Override
        public int height() {
            return image.getHeight();
        }

        @Override
        public void fetch(int x, int y, Vector4f result) {
            int rgba = image.getRGB(x, (height() - 1) - y);
            
            result.set(
                    Pixels.decodeNormalized(rgba, 1),
                    Pixels.decodeNormalized(rgba, 2),
                    Pixels.decodeNormalized(rgba, 3),
                    Pixels.decodeNormalized(rgba, 0)
            );
        }
    }
    
    public static Texture toTexture(BufferedImage image) {
        return new BufferedTexture(image);
    }
    
    public static BufferedImage fromTexture(Texture t) {
        if (t instanceof BufferedTexture e) {
            return e.getImage();
        }
        if (t == null) {
            throw new NullPointerException("Texture is null.");
        }
        
        int width = t.width();
        int height = t.height();
        BufferedImage data = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        Vector4f cache = new Vector4f();
        
        for (int i = 0; i < width*height; i++) {
            int x = i % width;
            int y = i / width;
            
            t.fetch(x, (height - y) - 1, cache);
            data.setRGB(x, y, Pixels.encodeNormalized(
                    cache.w(),
                    cache.x(),
                    cache.y(),
                    cache.z()
            ));
        }
        
        return data;
    }
    
    private AWTInterop() {
        
    }
}
