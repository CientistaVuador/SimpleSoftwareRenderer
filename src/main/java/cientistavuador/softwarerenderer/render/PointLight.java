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

import org.joml.Vector3f;

/**
 *
 * @author Cien
 */
public class PointLight implements Light {

    private final Vector3f diffuseColor = new Vector3f(0.8f, 0.8f, 0.8f);
    private final Vector3f ambientColor = new Vector3f(0.3f, 0.3f, 0.3f);
    private final Vector3f position = new Vector3f(0f, 0f, 0f);
    
    public PointLight() {
        
    }
    
    @Override
    public Vector3f getDiffuseColor() {
        return this.diffuseColor;
    }

    @Override
    public Vector3f getAmbientColor() {
        return this.ambientColor;
    }

    @Override
    public Vector3f getPosition() {
        return this.position;
    }

    @Override
    public void calculateDiffuseAmbientFactors(float x, float y, float z, float nx, float ny, float nz, float[] diffuseAmbientFactors) {
        float lightDirX = this.position.x() - x;
        float lightDirY = this.position.y() - y;
        float lightDirZ = this.position.z() - z;
        float lightDirLengthInverse = (float) (1.0 / Math.sqrt((lightDirX * lightDirX) + (lightDirY * lightDirY) + (lightDirZ * lightDirZ)));
        lightDirX *= lightDirLengthInverse;
        lightDirY *= lightDirLengthInverse;
        lightDirZ *= lightDirLengthInverse;
        diffuseAmbientFactors[0] = Math.max((lightDirX * nx) + (lightDirY * ny) + (lightDirZ * nz), 0f) * lightDirLengthInverse;
        diffuseAmbientFactors[1] = lightDirLengthInverse;
    }
    
}
