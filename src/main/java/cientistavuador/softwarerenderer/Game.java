/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cientistavuador.softwarerenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import cientistavuador.softwarerenderer.camera.FreeCamera;
import cientistavuador.softwarerenderer.render.AWTInterop;
import cientistavuador.softwarerenderer.render.Light;
import cientistavuador.softwarerenderer.render.PointLight;
import cientistavuador.softwarerenderer.render.Renderer;
import cientistavuador.softwarerenderer.render.SpotLight;
import cientistavuador.softwarerenderer.render.Surface;
import cientistavuador.softwarerenderer.render.Texture;
import cientistavuador.softwarerenderer.resources.ImageResources;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Exchanger;
import org.joml.Matrix4f;
import org.joml.Vector3f;

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

    private Throwable imageThreadException = null;
    private final Exchanger<Object> imageThreadExchanger = new Exchanger<>();
    private final Thread imageThread = new Thread(() -> {
        try {
            Surface surface = (Surface) this.imageThreadExchanger.exchange(null);
            while (true) {
                if (surface == null) {
                    surface = (Surface) this.imageThreadExchanger.exchange(null);
                    continue;
                }
                BufferedImage result = AWTInterop.fromTexture(surface.getColorBufferTexture());
                surface = (Surface) this.imageThreadExchanger.exchange(result);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }, "Image-Thread");

    private final float[] cottageVertices;
    private final Texture cottageTexture;
    private final Matrix4f cottageMatrix = new Matrix4f()
            .translate(82.61f, 59.5f, -30.05f)
            .scale(0.5f)
            .rotateY((float) Math.toRadians(45f));

    private final float[] terrainVertices;
    private final Texture terrainTexture;
    private final Matrix4f terrainMatrix = new Matrix4f()
            .scale(512f);

    private final float[] colaVertices;
    private final Texture colaTexture;
    private final Matrix4f colaMatrix = new Matrix4f();

    private final float[] lightIconVertices;
    private final Texture pointLightIcon;
    private final Texture spotLightIcon;
    private final Texture lightColorIcon;

    private boolean lightingEnabled = true;
    private boolean terrainEnabled = true;

    private Game() {
        //load 3d model, texture and model matrix
        this.cottageVertices = loadModel("cottage.obj");
        this.terrainVertices = loadModel("terrain.obj");
        this.colaVertices = loadModel("ciencola.obj");
        this.lightIconVertices = loadModel("billboard.obj");
        this.cottageTexture = this.renderer.imageToTexture(ImageResources.read("cottage_diffuse.png"));
        this.terrainTexture = this.renderer.imageToTexture(ImageResources.read("grass09.png"));
        this.colaTexture = this.renderer.imageToTexture(ImageResources.read("ciencola_diffuse.png"));
        this.pointLightIcon = this.renderer.imageToTexture(ImageResources.read("pointlight.png"));
        this.spotLightIcon = this.renderer.imageToTexture(ImageResources.read("spotlight.png"));
        this.lightColorIcon = this.renderer.imageToTexture(ImageResources.read("lightcolor.png"));
    }

    private float[] loadModel(String name) {
        try (BufferedReader reader
                = new BufferedReader(
                        new InputStreamReader(
                                ImageResources.class.getResourceAsStream(name),
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

            return this.renderer.finishVertices();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void start() {
        camera.setPosition(103.17f, 68.80f, -21.42f);
        camera.setRotation(0f, -180f, 0f);
        Thread currentThread = Thread.currentThread();
        this.imageThread.setUncaughtExceptionHandler((t, e) -> {
            this.imageThreadException = e;
            currentThread.interrupt();
        });
        this.imageThread.setDaemon(true);
        this.imageThread.start();
        this.renderer.setBilinearFilteringEnabled(false);

        this.renderer.getSunDiffuse().set(0.7f, 0.65f, 0.60f);

        SpotLight spot = new SpotLight();
        spot.getDiffuseColor().set(4f, 0.5f, 0.0f);
        spot.getAmbientColor().set(spot.getDiffuseColor()).mul(0.05f);
        spot.getPosition().set(83.70f, 65f, -6.82f);
        this.renderer.getLights().add(spot);

        PointLight point = new PointLight();
        point.getDiffuseColor().set(0.0f, 2f, 0.5f);
        point.getAmbientColor().set(point.getDiffuseColor()).mul(0.05f);
        point.getPosition().set(75.64f, 64.56f, -22.29f);
        this.renderer.getLights().add(point);
    }

    public void loop(Graphics2D g) {
        if (this.imageThreadException != null) {
            throw new RuntimeException("Exception in Image Thread", this.imageThreadException);
        }
        this.colaMatrix
                .identity()
                .translate(83.70f, 62f + Math.abs((this.rotation / 720f) - 0.25f), -6.82f)
                .rotateY((float) Math.toRadians(this.rotation))
                .rotateX((float) Math.toRadians(25f));
        this.rotation += Main.TPF * 30f;
        if (this.rotation > 360f) {
            this.rotation = 0f;
        }

        camera.updateMovement();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, 800, 600);

        this.renderer.clearBuffers();

        this.renderer.getProjection().set(this.camera.getProjection());
        this.renderer.getView().set(this.camera.getView());
        this.renderer.getCameraPosition().set(this.camera.getPosition());

        this.renderer.setLightingEnabled(this.lightingEnabled);
        
        int renderedVertices = 0;
        
        //terrain
        if (this.terrainEnabled) {
            this.renderer.setVertices(this.terrainVertices);
            this.renderer.getModel().set(this.terrainMatrix);
            this.renderer.setTexture(this.terrainTexture);
            
            renderedVertices = this.renderer.render();
            
            Main.NUMBER_OF_VERTICES += renderedVertices;
            Main.NUMBER_OF_DRAWCALLS++;
        }
        
        //cottage
        this.renderer.setVertices(this.cottageVertices);
        this.renderer.getModel().set(this.cottageMatrix);
        this.renderer.setTexture(this.cottageTexture);

        renderedVertices = this.renderer.render();

        Main.NUMBER_OF_VERTICES += renderedVertices;
        Main.NUMBER_OF_DRAWCALLS++;

        //cola
        this.renderer.setVertices(this.colaVertices);
        this.renderer.getModel().set(this.colaMatrix);
        this.renderer.setTexture(this.colaTexture);

        renderedVertices = this.renderer.render();

        Main.NUMBER_OF_VERTICES += renderedVertices;
        Main.NUMBER_OF_DRAWCALLS++;

        if (this.lightingEnabled) {
            //lights
            this.renderer.setVertices(this.lightIconVertices);
            this.renderer.setBillboardingEnabled(true);
            this.renderer.setLightingEnabled(false);
            for (Light light : this.renderer.getLights()) {
                this.renderer.getModel().identity().translate(light.getPosition());
                
                //icon
                if (light instanceof SpotLight) {
                    this.renderer.setTexture(this.spotLightIcon);
                } else {
                    this.renderer.setTexture(this.pointLightIcon);
                }
                renderedVertices = this.renderer.render();
                
                //overlay
                float lightR = light.getDiffuseColor().x();
                float lightG = light.getDiffuseColor().y();
                float lightB = light.getDiffuseColor().z();
                float largest = Math.max(Math.max(lightR, lightG), lightB);
                if (largest > 1f) {
                    lightR /= largest;
                    lightG /= largest;
                    lightB /= largest;
                }
                this.renderer.getColor().set(lightR, lightG, lightB, 1f);
                this.renderer.setTexture(this.lightColorIcon);
                renderedVertices += this.renderer.render();

                Main.NUMBER_OF_VERTICES += renderedVertices;
                Main.NUMBER_OF_DRAWCALLS += 2;

                this.renderer.getColor().set(1f);
            }
            this.renderer.setBillboardingEnabled(false);
            this.renderer.setLightingEnabled(true);
        }

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
                "  T - Hide This Wall of Text.",
                "  M - Multithread [" + (this.renderer.isMultithreadEnabled() ? "Enabled" : "Disabled") + "]",
                "  B - Bilinear Filtering [" + (this.renderer.isBilinearFilteringEnabled() ? "Enabled" : "Disabled") + "]",
                "  R - Resolution [" + this.renderer.getWidth() + "x" + this.renderer.getHeight() + "]",
                "  L - Lighting [" + (this.lightingEnabled ? "Enabled" : "Disabled") + "]",
                "  N - Terrain [" + (this.terrainEnabled ? "Enabled" : "Disabled") + "]"
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
        if (e.getKeyCode() == KeyEvent.VK_M && pressed) {
            this.renderer.setMultithreadEnabled(!this.renderer.isMultithreadEnabled());
        }
        if (e.getKeyCode() == KeyEvent.VK_B && pressed) {
            this.renderer.setBilinearFilteringEnabled(!this.renderer.isBilinearFilteringEnabled());
        }
        if (e.getKeyCode() == KeyEvent.VK_R && pressed) {
            try {
                this.imageThreadExchanger.exchange(null);
            } catch (InterruptedException ex) {
                if (this.imageThreadException != null) {
                    throw new RuntimeException("Exception in Image Thread", this.imageThreadException);
                } else {
                    throw new RuntimeException(ex);
                }
            }
            switch (this.renderer.getWidth()) {
                case 100 -> {
                    this.renderer.resize(200, 150);
                }
                case 200 -> {
                    this.renderer.resize(300, 225);
                }
                case 300 -> {
                    this.renderer.resize(400, 300);
                }
                case 400 -> {
                    this.renderer.resize(500, 375);
                }
                case 500 -> {
                    this.renderer.resize(600, 450);
                }
                case 600 -> {
                    this.renderer.resize(700, 525);
                }
                case 700 -> {
                    this.renderer.resize(800, 600);
                }
                case 800 -> {
                    this.renderer.resize(100, 75);
                }
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_L && pressed) {
            this.lightingEnabled = !this.lightingEnabled;
        }
        if (e.getKeyCode() == KeyEvent.VK_N && pressed) {
            this.terrainEnabled = !this.terrainEnabled;
        }
    }

    public void mouseCursorMoved(double x, double y) {
        camera.mouseCursorMoved(x, y);
    }

}
