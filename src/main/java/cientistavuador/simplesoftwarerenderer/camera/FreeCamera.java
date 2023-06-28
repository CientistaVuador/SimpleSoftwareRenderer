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
package cientistavuador.simplesoftwarerenderer.camera;

import cientistavuador.simplesoftwarerenderer.Main;

/**
 *
 * @author Shinoa Hiragi
 */
public class FreeCamera extends PerspectiveCamera {

    public static final float DEFAULT_SENSITIVITY = 0.1f;
    public static final float DEFAULT_RUN_SPEED = 5f;
    public static final float DEFAULT_SPEED = 2.5f;

    private float sensitivity = DEFAULT_SENSITIVITY;
    private float speed = DEFAULT_SPEED;
    private float runSpeed = DEFAULT_RUN_SPEED;

    //whatever it should capture the cursor or not.
    // press LeftControl in game to capture/release the cursor
    private boolean captureMouse = false;
    private boolean controlAlreadyPressed = false;
    
    //Movement control
    private boolean movementDisabled = false;

    public FreeCamera() {

    }

    //movimentation magic
    public void updateMovement() {
        if (isControlPressedOnce()) {
            this.captureMouse = !this.captureMouse;
            Main.HIDE_CURSOR = this.captureMouse;
            System.out.println("Free Camera capture state: " + this.captureMouse);
        }

        if (isMovementDisabled()) {
            return;
        }

        int directionX = 0;
        int directionZ = 0;

        if (Main.W_PRESSED) {
            directionZ += 1;
        }
        if (Main.S_PRESSED) {
            directionZ += -1;
        }
        if (Main.A_PRESSED) {
            directionX += 1;
        }
        if (Main.D_PRESSED) {
            directionX += -1;
        }

        float diagonal = (Math.abs(directionX) == 1 && Math.abs(directionZ) == 1) ? 0.707106781186f : 1f;
        float currentSpeed = Main.SHIFT_PRESSED ? runSpeed : speed;
        if (Main.ALT_PRESSED) {
            currentSpeed /= 4f;
        }

        //acceleration in X and Z axis
        float xa = currentSpeed * diagonal * directionX;
        float za = currentSpeed * diagonal * directionZ;

        super.setPosition(
                super.getPosition().x() + ((super.getRight().x() * xa + super.getFront().x() * za) * Main.TPF),
                super.getPosition().y() + ((super.getRight().y() * xa + super.getFront().y() * za) * Main.TPF),
                super.getPosition().z() + ((super.getRight().z() * xa + super.getFront().z() * za) * Main.TPF)
        );
    }

    // rotates camera using the cursor's position
    public void mouseCursorMoved(double mx, double my) {
        if (captureMouse) {
            super.setRotation(
                    super.getRotation().x() + (float) (my * sensitivity),
                    super.getRotation().y() + (float) (mx * sensitivity),
                    0
            );

            if (super.getRotation().x() >= 90) {
                super.setRotation(89.9f, super.getRotation().y(), 0);
            }
            if (super.getRotation().x() <= -90) {
                super.setRotation(-89.9f, super.getRotation().y(), 0);
            }
        }
    }

    /*
    * May be a little of Overengineering by me, but, here's the idea: it only
    * returns true one time if the left control key is pressed, it won't return
    * true again until that key is released; and pressed again,
     */
    private boolean isControlPressedOnce() {
        if (Main.CTRL_PRESSED) {
            if (!controlAlreadyPressed) {
                controlAlreadyPressed = true;
                return true;
            }
            return false;
        }
        if (!Main.CTRL_PRESSED) {
            controlAlreadyPressed = false;
        }
        return false;
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }

    public float getSensitivity() {
        return sensitivity;
    }

    public void setRunSpeed(float runSpeed) {
        this.runSpeed = runSpeed;
    }

    public float getRunSpeed() {
        return this.runSpeed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public boolean isMovementDisabled() {
        return movementDisabled;
    }

    public void setMovementDisabled(boolean movementDisabled) {
        this.movementDisabled = movementDisabled;
    }

}
