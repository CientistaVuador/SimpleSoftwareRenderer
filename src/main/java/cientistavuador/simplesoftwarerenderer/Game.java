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
import cientistavuador.simplesoftwarerenderer.render.AWTInterop;
import cientistavuador.simplesoftwarerenderer.render.Rasterizer;
import cientistavuador.simplesoftwarerenderer.render.Surface;
import cientistavuador.simplesoftwarerenderer.render.Texture;
import cientistavuador.simplesoftwarerenderer.render.VerticesBuilder;
import cientistavuador.simplesoftwarerenderer.render.VerticesProcessor;
import cientistavuador.simplesoftwarerenderer.resources.image.ImageResources;
import java.awt.Font;
import java.awt.image.BufferedImage;
import org.joml.Matrix4f;

/**
 *
 * @author cientista
 */
public class Game {

    private static final Game GAME = new Game();

    public static Game get() {
        return GAME;
    }

    private final Font BIG_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 26);
    private final Font SMALL_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    private boolean textEnabled = true;
    private final FreeCamera camera = new FreeCamera();
    private final Surface surface = new Surface();
    private final Texture texture = AWTInterop.toTexture(ImageResources.read("pointlight.png"));
    private BufferedImage outputImage;
    private final float[] vertices;

    private Game() {
        VerticesBuilder stream = new VerticesBuilder();
        
        int leftDown = stream.position(-0.5f, -0.5f, 0.0f);
        int rightDown = stream.position(0.5f, -0.5f, 0.0f);
        int rightUp = stream.position(0.5f, 0.5f, 0.0f);
        int leftUp = stream.position(-0.5f, 0.5f, 0.0f);
        
        int leftDownUv = stream.texture(0.0f + 0.0078125f, 0.0f + 0.0078125f);
        int rightDownUv = stream.texture(1.0f - 0.0078125f, 0.0f + 0.0078125f);
        int rightUpUv = stream.texture(1.0f - 0.0078125f, 1.0f - 0.0078125f);
        int leftUpUv = stream.texture(0.0f + 0.0078125f, 1.0f - 0.0078125f);
        
        stream.vertex(leftDown, leftDownUv, 0, 0);
        stream.vertex(rightDown, rightDownUv, 0, 0);
        stream.vertex(rightUp, rightUpUv, 0, 0);
        
        stream.vertex(leftDown, leftDownUv, 0, 0);
        stream.vertex(rightUp, rightUpUv, 0, 0);
        stream.vertex(leftUp, leftUpUv, 0, 0);
        
        this.vertices = stream.vertices();
    }

    public void start() {
        camera.setPosition(0, 0, 1f);
    }

    public void loop(Graphics2D g) {
        camera.updateMovement();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 600);
        
        surface.clearColor(0.2f, 0.4f, 0.6f);
        
        VerticesProcessor processor = new VerticesProcessor(this.vertices, new Matrix4f(this.camera.getProjectionView()), null);
        Rasterizer rasterizer = new Rasterizer(surface, this.texture, processor.process());
        rasterizer.render();
        
        outputImage = AWTInterop.fromTexture(surface.getColorBufferTexture());
        g.drawImage(outputImage, 0, 0, Main.WIDTH, Main.HEIGHT, null);

        if (this.textEnabled) {
            g.setFont(BIG_FONT);
            g.setColor(Color.YELLOW);
            g.drawString("SimpleSoftwareRenderer", 0, BIG_FONT.getSize());
            
            String[] wallOfText = {
                "FPS: " + Main.FPS,
                "X: " + format(camera.getPosition().x()),
                "Y: " + format(camera.getPosition().y()),
                "Z: " + format(camera.getPosition().z()),
                "Controls:",
                "  WASD + Space + Mouse - Move",
                "  Shift - Run",
                "  Alt - Wander",
                "  Ctrl - Unlock/Lock mouse",
                "  T - Hide This Wall of Text."
            };
            
            int offset = SMALL_FONT.getSize();
            int offsetBig = BIG_FONT.getSize();
            g.setFont(SMALL_FONT);
            g.setColor(Color.WHITE);
            for (int i = 0; i < wallOfText.length; i++) {
                g.drawString(wallOfText[i], 0, (offset * i) + (offsetBig * 2));
            }
        }

        Main.WINDOW_TITLE += " (DrawCalls: " + Main.NUMBER_OF_DRAWCALLS + ", Vertices: " + Main.NUMBER_OF_VERTICES + ")";
        Main.WINDOW_TITLE += " (x:" + (int) Math.floor(camera.getPosition().x()) + ",y:" + (int) Math.floor(camera.getPosition().y()) + ",z:" + (int) Math.ceil(camera.getPosition().z()) + ")";
        if (!this.textEnabled) {
            Main.WINDOW_TITLE += " (T - Show Wall of Text)";
        }
    }

    private String format(double d) {
        return String.format("%.2f", d);
    }

    public void keyCallback(KeyEvent e, boolean pressed) {
        if (e.getKeyCode() == KeyEvent.VK_T && pressed) {
            this.textEnabled = !this.textEnabled;
        }
    }

    public void mouseCursorMoved(double x, double y) {
        camera.mouseCursorMoved(x, y);
    }

}
