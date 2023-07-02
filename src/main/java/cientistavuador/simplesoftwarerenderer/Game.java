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
import cientistavuador.simplesoftwarerenderer.debug.DebugCounter;
import cientistavuador.simplesoftwarerenderer.render.AWTInterop;
import cientistavuador.simplesoftwarerenderer.render.Renderer;
import cientistavuador.simplesoftwarerenderer.render.Surface;
import cientistavuador.simplesoftwarerenderer.resources.ImageResources;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Exchanger;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private final Renderer renderer = Renderer.create(200, 150);
    private float rotation = 0f;
    
    private Throwable imageThreadException = null;
    private final Exchanger<Object> imageThreadExchanger = new Exchanger<>();
    private final Thread imageThread = new Thread(() -> {
        try {
            Surface surface = (Surface) this.imageThreadExchanger.exchange(null);
            while (true) {
                BufferedImage result = AWTInterop.fromTexture(surface.getColorBufferTexture());
                surface = (Surface) this.imageThreadExchanger.exchange(result);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }, "Image-Thread");

    private Game() {
        //load 3d model, texture and model matrix
        loadCottage();
        this.renderer.setTexture(this.renderer.imageToTexture(ImageResources.read("cottage_diffuse.png")));
        this.renderer.setModel(
                new Matrix4f()
                        .translate(0f, -2f, -7f)
                        .scale(0.25f)
                        .rotateY((float) Math.toRadians(45f))
        );
    }

    private void loadCottage() {
        try (BufferedReader reader
                = new BufferedReader(
                        new InputStreamReader(
                                ImageResources.class.getResourceAsStream("cottage.obj"),
                                StandardCharsets.UTF_8
                        )
                )) {

            this.renderer.beginVertices();

            String s;
            while ((s = reader.readLine()) != null) {
                if (s.startsWith("#") || s.isBlank()) {
                    continue;
                }
                String[] split = s.split(" ");

                switch (split[0]) {
                    case "v" -> {
                        float x = Float.parseFloat(split[1]);
                        float y = Float.parseFloat(split[2]);
                        float z = Float.parseFloat(split[3]);
                        this.renderer.position(x, y, z);
                    }
                    case "vt" -> {
                        float u = Float.parseFloat(split[1]);
                        float v = Float.parseFloat(split[2]);
                        this.renderer.texture(u, v);
                    }
                    case "vn" -> {
                        float nx = Float.parseFloat(split[1]);
                        float ny = Float.parseFloat(split[2]);
                        float nz = Float.parseFloat(split[3]);
                        this.renderer.normal(nx, ny, nz);
                    }
                    case "f" -> {
                        for (int i = 0; i < 3; i++) {
                            String[] faceSplit = split[1 + i].split("/");
                            int position = Integer.parseInt(faceSplit[0]);
                            int uv = Integer.parseInt(faceSplit[1]);
                            int normal = Integer.parseInt(faceSplit[2]);
                            this.renderer.vertex(position, uv, normal, 0);
                        }
                    }
                }

            }

            this.renderer.finishVerticesAndSet();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void start() {
        camera.setPosition(0, 0, 1f);
        Thread currentThread = Thread.currentThread();
        this.imageThread.setUncaughtExceptionHandler((t, e) -> {
            this.imageThreadException = e;
            currentThread.interrupt();
        });
        this.imageThread.setDaemon(true);
        this.imageThread.start();
    }

    public void loop(Graphics2D g) {
        if (this.imageThreadException != null) {
            throw new RuntimeException("Exception in Image Thread", this.imageThreadException);
        }

        camera.updateMovement();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 600);
        
        this.renderer.clearBuffers();

        this.renderer.setProjectionView(new Matrix4f(this.camera.getProjectionView()));

        int renderedVertices = this.renderer.render();

        Main.NUMBER_OF_VERTICES += renderedVertices;
        Main.NUMBER_OF_DRAWCALLS++;

        Matrix4f otherModel = this.renderer.getModel();

        this.renderer.setModel(
                new Matrix4f()
                        .translate(-6f, -2f, -1f)
                        .scale(0.05f)
                        .rotateY((float) Math.toRadians(this.rotation))
        );
        this.rotation += Main.TPF * 12f;
        if (this.rotation > 360f) {
            this.rotation = 0f;
        }
        
        renderedVertices = this.renderer.render();

        Main.NUMBER_OF_VERTICES += renderedVertices;
        Main.NUMBER_OF_DRAWCALLS++;

        this.renderer.setModel(otherModel);

        try {
            BufferedImage e = (BufferedImage) this.imageThreadExchanger.exchange(this.renderer.getSurface());

            this.renderer.flipSurfaces();

            g.drawImage(e, 0, 0, Main.WIDTH, Main.HEIGHT, null);
        } catch (InterruptedException ex) {
            if (this.imageThreadException != null) {
                throw new RuntimeException("Exception in Image Thread", this.imageThreadException);
            } else {
                throw new RuntimeException(ex);
            }
        }

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
