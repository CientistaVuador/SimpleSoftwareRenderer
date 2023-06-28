/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cientistavuador.simplesoftwarerenderer;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 *
 * @author cientista
 */
public class Camera {

    public static final float GRAVITY = 9.8f / 2f;
    public static final float JUMP_SPEED = 1.5f;

    public static final float YAW = 90.0f;
    public static final float PITCH = 0.0f;
    public static final float SPEED = 0.85f;
    public static final float SENSITIVITY = 0.05f;
    public static final float FOV = 90f;

    //recycled math objects
    private static final Vector3f stackA = new Vector3f();
    private static final Vector3f stackB = new Vector3f();
    //

    private float speedY = 0;
    
    private final Vector3f up = new Vector3f();
    private final Vector3f right = new Vector3f();

    private final Vector3f position = new Vector3f();
    private final Vector3f worldUp = new Vector3f();

    private final Vector3f front = new Vector3f(0.0f, 0.0f, -1.0f);

    private float yaw;
    private float pitch;
    private float fov = FOV;

    private final float movementSpeed = SPEED;
    private final float mouseSensitivity = SENSITIVITY;

    public Camera(float posX, float posY, float posZ, float upX, float upY, float upZ, float yaw, float pitch) {
        this.position.set(posX, posY, posZ);
        this.worldUp.set(upX, upY, upZ);
        this.yaw = yaw;
        this.pitch = pitch;
        updateCameraVectors();
    }

    public Camera(Vector3f position, Vector3f up, float yaw, float pitch) {
        this(position.x(), position.y(), position.z(), up.x(), up.y(), up.z(), yaw, pitch);
    }

    public Camera(Vector3f position, Vector3f up, float yaw) {
        this(position, up, yaw, PITCH);
    }

    public Camera(Vector3f position, Vector3f up) {
        this(position, up, YAW);
    }

    public Camera(Vector3f position) {
        this(position, new Vector3f(0.0f, 1.0f, 0.0f));
    }

    public Camera() {
        this(new Vector3f(0, 3, 0));
    }

    private void updateCameraVectors() {
        front.set(
                cos(toRadians(yaw)) * cos(toRadians(pitch)),
                sin(toRadians(pitch)),
                sin(toRadians(yaw)) * cos(toRadians(pitch))
        ).normalize();

        right.set(front.cross(worldUp, stackA)).normalize();
        up.set(right.cross(front, stackB)).normalize();
    }

    public Matrix4f getView(Matrix4f receiver) {
        receiver
                .identity()
                .lookAt(position,
                        position.add(front, stackA),
                        up);
        return receiver;
    }

    public Matrix4f getProjection(Matrix4f receiver) {
        receiver.identity().perspective(
                (float) toRadians(fov),
                800f / 600f,
                0.001f,
                100.0f
        );
        return receiver;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void resetPosition() {
        position.set(0, 5, 0);
    }
    
    public Platform processKeyboard(Level level, float tpf, boolean w, boolean a, boolean s, boolean d, boolean shift, boolean space) {
        Platform collision = level.getCollision(this);
        
        Vector3f walkTranslate = Camera.stackA.zero();
        float multiplier = 1;

        if (w) {
            walkTranslate.add(1, 0, 0);
        }

        if (s) {
            walkTranslate.add(-1, 0, 0);
        }

        if (a) {
            walkTranslate.add(0, 0, -1);
        }

        if (d) {
            walkTranslate.add(0, 0, 1);
        }

        if (shift) {
            multiplier = 2;
        }

        walkTranslate
                .normalize()
                .rotateY((float) toRadians(-yaw))
                .mul(movementSpeed * tpf * multiplier);

        if (walkTranslate.isFinite()) {
            if (collision != null) {
                position.add(walkTranslate);
                return collision;
            }
            
            float lastPosX = position.x();
            float lastPosY = position.y();
            float lastPosZ = position.z();
            
            position.add(walkTranslate.x(), 0, 0);
            collision = level.getCollision(this);
            if (collision != null) {
                position.setComponent(0, lastPosX);
            }
            
            position.add(0, walkTranslate.y(), 0);
            collision = level.getCollision(this);
            if (collision != null) {
                position.setComponent(1, lastPosY);
            }
            
            position.add(0, 0, walkTranslate.z());
            collision = level.getCollision(this);
            if (collision != null) {
                position.setComponent(2, lastPosZ);
            }
        }
        
        if (space && speedY == 0) {
            speedY = JUMP_SPEED;
        }
        
        float lastPosY = position.y();
        
        speedY += GRAVITY * -tpf;
        float yMove = speedY * tpf;
        position.add(0, yMove, 0);
        collision = level.getCollision(this);
        if (collision != null) {
            speedY = 0;
            position.setComponent(1, lastPosY);
        }
        
        return collision;
    }

    public void processMouseMovement(float xPos, float yPos, boolean constrainPitch) {
        xPos *= mouseSensitivity;
        yPos *= mouseSensitivity;

        yaw += xPos;
        pitch += yPos;

        if (constrainPitch) {
            if (pitch > 89.0f) {
                pitch = 89.0f;
            } else if (pitch < -89.0f) {
                pitch = -89.0f;
            }
        }

        updateCameraVectors();
    }

    public void processMouseMovement(float xOffset, float yOffset) {
        processMouseMovement(xOffset, yOffset, true);
    }

    public Vector3f getFront() {
        return front;
    }

    public Vector3f getUp() {
        return up;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        updateCameraVectors();
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        updateCameraVectors();
    }

    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

}
