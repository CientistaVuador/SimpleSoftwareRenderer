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
package cientistavuador.simplesoftwarerenderer.util;

/**
 * old code from an old project.
 * @author Cien
 */
public class Pixels {
    
    public static final int NUMBER_OF_CHANNELS = 4;
    public static final int R = 0;
    public static final int G = 1;
    public static final int B = 2;
    public static final int A = 3;
    
    private static byte floatToByte(float e) {
        return (byte) Math.min(Math.max(e * 255f, 0f), 255f);
    }
    
    private static float byteToFloat(byte e) {
        return ((e & 0xFF) / 255f);
    }
    
    public static int encode(byte r, byte g, byte b, byte a) {
        return ((r & 0xFF) << 24)
                | ((g & 0xFF) << 16)
                | ((b & 0xFF) << 8)
                | ((a & 0xFF) << 0)
                ;
    }
    
    public static int encode(byte r, byte g, byte b) {
        return encode(r, g, b, (byte) 255);
    }
    
    public static int encode(byte r, byte g) {
        return encode(r, g, (byte) 0);
    }
    
    public static int encode(byte r) {
        return encode(r, (byte) 0);
    }
    
    public static byte decode(int pixel, int channel) {
        switch (channel) {
            case 0 -> {
                return (byte) (pixel >>> 24);
            }
            case 1 -> {
                return (byte) (pixel >>> 16);
            }
            case 2 -> {
                return (byte) (pixel >>> 8);
            }
            case 3 -> {
                return (byte) (pixel >>> 0);
            }
            default -> throw new IllegalArgumentException("Invalid channel index "+channel+" must be 0-3");
        }
    }
    
    public static int encodeNormalized(float r, float g, float b, float a) {
        return encode(floatToByte(r), floatToByte(g), floatToByte(b), floatToByte(a));
    }
    
    public static int encodeNormalized(float r, float g, float b) {
        return encode(floatToByte(r), floatToByte(g), floatToByte(b));
    }
    
    public static int encodeNormalized(float r, float g) {
        return encode(floatToByte(r), floatToByte(g));
    }
    
    public static int encodeNormalized(float r) {
        return encode(floatToByte(r));
    }
    
    public static float decodeNormalized(int pixel, int channel) {
        return byteToFloat(decode(pixel, channel));
    }
    
    public static int encodeInt(int r, int g, int b, int a) {
        return encode((byte) r, (byte) g, (byte) b, (byte) a);
    }
    
    public static int encodeInt(int r, int g, int b) {
        return encode((byte) r, (byte) g, (byte) b);
    }
    
    public static int encodeInt(int r, int g) {
        return encode((byte) r, (byte) g);
    }
    
    public static int encodeInt(int r) {
        return encode((byte) r);
    }
    
    public static int decodeInt(int pixel, int channel) {
        return Byte.toUnsignedInt(decode(pixel, channel));
    }
    
    public static int mix(int pixel0, int pixel1, float mixValue) {
        float r0 = decodeNormalized(pixel0, Pixels.R);
        float g0 = decodeNormalized(pixel0, Pixels.G);
        float b0 = decodeNormalized(pixel0, Pixels.B);
        float a0 = decodeNormalized(pixel0, Pixels.A);
        
        float r1 = decodeNormalized(pixel1, Pixels.R);
        float g1 = decodeNormalized(pixel1, Pixels.G);
        float b1 = decodeNormalized(pixel1, Pixels.B);
        float a1 = decodeNormalized(pixel1, Pixels.A);
        
        float rM = Math.fma(r1 - r0, mixValue, r0);
        float gM = Math.fma(g1 - g0, mixValue, g0);
        float bM = Math.fma(b1 - b0, mixValue, b0);
        float aM = Math.fma(a1 - a0, mixValue, a0);
        
        return encodeNormalized(rM, gM, bM, aM);
    }
    
    public static int toGammaSpace(int pixel, float gamma) {
        float r = decodeNormalized(pixel, Pixels.R);
        float g = decodeNormalized(pixel, Pixels.G);
        float b = decodeNormalized(pixel, Pixels.B);
        float a = decodeNormalized(pixel, Pixels.A);
        
        float invGamma = 1f / gamma;
        
        r = (float) Math.pow(r, invGamma);
        g = (float) Math.pow(g, invGamma);
        b = (float) Math.pow(b, invGamma);
        
        return Pixels.encodeNormalized(r, g, b, a);
    }
    
    public static int toLinearSpace(int pixel, float gamma) {
        float r = decodeNormalized(pixel, Pixels.R);
        float g = decodeNormalized(pixel, Pixels.G);
        float b = decodeNormalized(pixel, Pixels.B);
        float a = decodeNormalized(pixel, Pixels.A);
        
        r = (float) Math.pow(r, gamma);
        g = (float) Math.pow(g, gamma);
        b = (float) Math.pow(b, gamma);
        
        return Pixels.encodeNormalized(r, g, b, a);
    }
    
    public static String toString(int pixel) {
        return "R:"+decodeInt(pixel, Pixels.R)+",G:"+decodeInt(pixel, Pixels.G)+",B:"+decodeInt(pixel, Pixels.B)+",A:"+decodeInt(pixel, Pixels.A);
    }
    
    private Pixels() {
        throw new RuntimeException();
    }
}
