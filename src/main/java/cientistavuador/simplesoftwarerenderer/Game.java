/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cientistavuador.simplesoftwarerenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import org.joml.Vector3f;
import cientistavuador.simplesoftwarerenderer.Platform.EndLevelPlatform;
import cientistavuador.simplesoftwarerenderer.Platform.UpdatablePlatform;

/**
 *
 * @author cientista
 */
public class Game {

    private static final Game GAME = new Game();

    private Camera cam = new Camera();
    private int currentLevel = 0;
    private Level level = new Level(10);
    
    public static Game get() {
        return GAME;
    }

    private Game() {

    }

    public void start() {
        
    }

    public void loop(Graphics2D g) {
        Platform collision = this.cam.processKeyboard(this.level, 
                (float) Main.TPF,
                Main.W_PRESSED,
                Main.A_PRESSED,
                Main.S_PRESSED,
                Main.D_PRESSED,
                Main.SHIFT_PRESSED,
                Main.SPACE_PRESSED
        );

        if (collision != null && collision instanceof EndLevelPlatform) {
            currentLevel++;
            level = new Level(10 + (currentLevel * 5));
            cam.resetPosition();
        }

        float yStore = cam.getPosition().get(1);
        cam.getPosition().add(0, -0.005f, 0);
        collision = level.getCollision(cam);
        cam.getPosition().setComponent(1, yStore);
        if (collision instanceof UpdatablePlatform) {
            Vector3f pos = ((UpdatablePlatform) collision).getPosition();
            Vector3f lastPos = ((UpdatablePlatform) collision).getLastPosition();

            float translateX = pos.x() - lastPos.x();
            float translateY = pos.y() - lastPos.y();
            float translateZ = pos.z() - lastPos.z();

            cam.getPosition().add(translateX, translateY, translateZ);
        }

        cam.processMouseMovement(Main.MOUSE_POS_X, Main.MOUSE_POS_Y);

        if (cam.getPosition().y() < -5f) {
            cam.resetPosition();
        }

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 600);
        
        g.setColor(Color.WHITE);
        g.drawString("FPS: " + Main.FPS, 0, 14);

        g.drawString("Level: " + currentLevel, 0, 28);

        level.render(g, cam, (float) Main.TPF);
    }
    
    public void keyCallback(KeyEvent e, boolean pressed) {
        
    }

}
