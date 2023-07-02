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
import cientistavuador.simplesoftwarerenderer.render.Renderer;
import cientistavuador.simplesoftwarerenderer.resources.ImageResources;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

    private final Renderer renderer = Renderer.create(400, 300);
    private float rotation = 0f;

    private final DebugCounter counter = new DebugCounter();
    
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
        
        counter.setTimerAction(() -> {
            counter.print();
            counter.clear();
        });
        counter.setTimerActionInterval(3000);
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
    }

    public void loop(Graphics2D g) {
        camera.updateMovement();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 600);

        counter.markStart("Clear Buffers");
        this.renderer.clearBuffers();
        counter.markEnd("Clear Buffers");
        
        this.renderer.setProjectionView(new Matrix4f(this.camera.getProjectionView()));
        
        counter.markStart("Render House");
        int renderedVertices = this.renderer.render();
        counter.markEnd("Render House");

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

        counter.markStart("Render Tiny House");
        renderedVertices = this.renderer.render();
        counter.markEnd("Render Tiny House");

        Main.NUMBER_OF_VERTICES += renderedVertices;
        Main.NUMBER_OF_DRAWCALLS++;

        this.renderer.setModel(otherModel);
        
        counter.markStart("Color Buffer to Image");
        BufferedImage e = this.renderer.colorBufferToImage();
        counter.markEnd("Color Buffer to Image");
        
        counter.markStart("Output");
        g.drawImage(e, 0, 0, Main.WIDTH, Main.HEIGHT, null);
        counter.markEnd("Output");

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
