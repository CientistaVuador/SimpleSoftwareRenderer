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
public class SpotLight extends PointLight {

    private static final float cutOffAmbientCosRad = (float) Math.cos(Math.toRadians(160f));
    private static final float outerCutOffAmbientCosRad = (float) Math.cos(Math.toRadians(180f));
    
    private final Vector3f direction = new Vector3f(0f, -1f, 0f);
    private float cutOff = 10f;
    private float outerCutOff = 45f;
    
    private float cutOffCosRad = (float) Math.cos(Math.toRadians(this.cutOff));
    private float outerCutOffCosRad = (float) Math.cos(Math.toRadians(this.outerCutOff));
    
    public SpotLight() {
        
    }

    public Vector3f getDirection() {
        return direction;
    }

    public float getCutOff() {
        return cutOff;
    }

    public float getOuterCutOff() {
        return outerCutOff;
    }
    
    public void setCutOff(float cutOff) {
        this.cutOff = cutOff;
        this.cutOffCosRad = (float) Math.cos(Math.toRadians(this.cutOff));
    }

    public void setOuterCutOff(float outerCutOff) {
        this.outerCutOff = outerCutOff;
        this.outerCutOffCosRad = (float) Math.cos(Math.toRadians(this.outerCutOff));
    }

    @Override
    public void calculateDiffuseAmbientFactors(float x, float y, float z, float nx, float ny, float nz, float[] diffuseAmbientFactors) {
        float lightDirX = getPosition().x() - x;
        float lightDirY = getPosition().y() - y;
        float lightDirZ = getPosition().z() - z;
        float lightDirLengthInverse = (float) (1.0 / Math.sqrt((lightDirX * lightDirX) + (lightDirY * lightDirY) + (lightDirZ * lightDirZ)));
        lightDirX *= lightDirLengthInverse;
        lightDirY *= lightDirLengthInverse;
        lightDirZ *= lightDirLengthInverse;
        
        float theta = (lightDirX * -this.direction.x()) + (lightDirY * -this.direction.y()) + (lightDirZ * -this.direction.z());
        
        float epsilonDiffuse = this.cutOffCosRad - this.outerCutOffCosRad;
        float intensityDiffuse = Math.min(Math.max((theta - this.outerCutOffCosRad) / epsilonDiffuse, 0.0f), 1.0f);
        
        float epsilonAmbient = SpotLight.cutOffAmbientCosRad - SpotLight.outerCutOffAmbientCosRad;
        float intensityAmbient = Math.min(Math.max((theta - SpotLight.outerCutOffAmbientCosRad) / epsilonAmbient, 0.0f), 1.0f);
        
        diffuseAmbientFactors[0] = Math.max((lightDirX * nx) + (lightDirY * ny) + (lightDirZ * nz), 0f) * lightDirLengthInverse * intensityDiffuse;
        diffuseAmbientFactors[1] = lightDirLengthInverse * intensityAmbient;
    }
    
}
