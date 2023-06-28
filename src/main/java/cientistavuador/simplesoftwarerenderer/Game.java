/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cientistavuador.simplesoftwarerenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import cientistavuador.simplesoftwarerenderer.camera.FreeCamera;

/**
 *
 * @author cientista
 */
public class Game {

    private static final Game GAME = new Game();

    private FreeCamera cam = new FreeCamera();
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
        cam.updateMovement();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 600);
        
        g.setColor(Color.WHITE);
        g.drawString("FPS: " + Main.FPS, 0, 14);

        g.drawString("Level: " + currentLevel, 0, 28);
        
        g.drawLine(0, 599, 799, 599);
        
        level.render(g, cam, (float) Main.TPF);
    }
    
    public void keyCallback(KeyEvent e, boolean pressed) {
        
    }

    public void mouseCursorMoved(double x, double y) {
        cam.mouseCursorMoved(x, y);
    }
    
}
