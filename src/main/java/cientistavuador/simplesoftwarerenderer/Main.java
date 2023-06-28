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
package cientistavuador.simplesoftwarerenderer;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.joml.Vector3f;
import static org.lwjgl.system.MemoryUtil.*;

/**
 * Main class
 *
 * @author Cien
 */
public class Main {

    public static final boolean USE_MSAA = false;
    public static final boolean DEBUG_ENABLED = true;
    public static final boolean SPIKE_LAG_WARNINGS = false;

    static {
        org.lwjgl.system.Configuration.LIBRARY_PATH.set("natives");
    }

    public static class OpenGLErrorException extends RuntimeException {

        private static final long serialVersionUID = 1L;
        private final int error;

        public OpenGLErrorException(int error) {
            super("OpenGL Error " + error);
            this.error = error;
        }

        public int getError() {
            return error;
        }
    }

    public static class GLFWErrorException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public GLFWErrorException(String error) {
            super(error);
        }
    }

    public static boolean THROW_GL_GLFW_ERRORS = true;

    public static String WINDOW_TITLE = "CienCraft - FPS: 60";
    public static int WIDTH = 800;
    public static int HEIGHT = 600;
    public static double TPF = 1 / 60d;
    public static int FPS = 60;
    public static long WINDOW_POINTER = NULL;
    public static long FRAME = 0;
    public static double ONE_SECOND_COUNTER = 0.0;
    public static double ONE_MINUTE_COUNTER = 0.0;
    public static int NUMBER_OF_DRAWCALLS = 0;
    public static int NUMBER_OF_VERTICES = 0;
    public static final ConcurrentLinkedQueue<Runnable> MAIN_TASKS = new ConcurrentLinkedQueue<>();
    public static final Vector3f DEFAULT_CLEAR_COLOR = new Vector3f(0.2f, 0.4f, 0.6f);

    public static boolean W_PRESSED = false;
    public static boolean A_PRESSED = false;
    public static boolean S_PRESSED = false;
    public static boolean D_PRESSED = false;
    public static boolean SHIFT_PRESSED = false;
    public static boolean SPACE_PRESSED = false;
    public static boolean ALT_PRESSED = false;
    public static boolean CTRL_PRESSED = false;

    public static int MOUSE_POS_X = 0;
    public static int MOUSE_POS_Y = 0;
    
    public static boolean HIDE_CURSOR = false;
    public static boolean CURSOR_HIDDEN = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Robot robot;
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            throw new RuntimeException(ex);
        }

        JFrame frame = new JFrame("Parkour Game");
        frame.setSize(Main.WIDTH, Main.HEIGHT);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        frame.add(canvas);

        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W ->
                        W_PRESSED = true;
                    case KeyEvent.VK_A ->
                        A_PRESSED = true;
                    case KeyEvent.VK_S ->
                        S_PRESSED = true;
                    case KeyEvent.VK_D ->
                        D_PRESSED = true;
                    case KeyEvent.VK_SHIFT ->
                        SHIFT_PRESSED = true;
                    case KeyEvent.VK_SPACE ->
                        SPACE_PRESSED = true;
                    case KeyEvent.VK_ALT ->
                        ALT_PRESSED = true;
                    case KeyEvent.VK_CONTROL ->
                        CTRL_PRESSED = true;
                    case KeyEvent.VK_ESCAPE ->
                        System.exit(0);
                }
                Game.get().keyCallback(e, true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W ->
                        W_PRESSED = false;
                    case KeyEvent.VK_A ->
                        A_PRESSED = false;
                    case KeyEvent.VK_S ->
                        S_PRESSED = false;
                    case KeyEvent.VK_D ->
                        D_PRESSED = false;
                    case KeyEvent.VK_SHIFT ->
                        SHIFT_PRESSED = false;
                    case KeyEvent.VK_SPACE ->
                        SPACE_PRESSED = false;
                    case KeyEvent.VK_ALT ->
                        ALT_PRESSED = false;
                    case KeyEvent.VK_CONTROL ->
                        CTRL_PRESSED = false;
                }
                Game.get().keyCallback(e, false);
            }
        });

        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!canvas.isFocusOwner()) {
                    return;
                }

                int centerX = frame.getX() + (Main.WIDTH / 2);
                int centerY = frame.getY() + (Main.HEIGHT / 2);

                int screenX = e.getXOnScreen();
                int screenY = e.getYOnScreen();
                
                MOUSE_POS_X = screenX - centerX;
                MOUSE_POS_Y = centerY - screenY;
                
                Game.get().mouseCursorMoved(MOUSE_POS_X, MOUSE_POS_Y);
                
                if (Main.CURSOR_HIDDEN) {
                    robot.mouseMove(centerX, centerY);
                }
            }
        });

        frame.setVisible(true);

        frame.setSize(
                frame.getWidth() + (frame.getInsets().left + frame.getInsets().right),
                frame.getHeight() + (frame.getInsets().top + frame.getInsets().bottom)
        );

        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

        //canvas.setCursor(blankCursor);
        canvas.requestFocus();
        
        try {
            SwingUtilities.invokeAndWait(() -> {
                Game.get(); //static initialize

                Game.get().start();
            });
        } catch (InterruptedException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }

        int frames = 0;
        long nextFpsUpdate = System.currentTimeMillis() + 1000;
        long nextTitleUpdate = System.currentTimeMillis() + 100;
        long timeFrameBegin = System.nanoTime();

        while (true) {
            Main.TPF = (System.nanoTime() - timeFrameBegin) / 1E9d;
            timeFrameBegin = System.nanoTime();

            Main.NUMBER_OF_DRAWCALLS = 0;
            Main.NUMBER_OF_VERTICES = 0;
            Main.WINDOW_TITLE = "BakedLightingExperiment - FPS: " + Main.FPS;

            if (SPIKE_LAG_WARNINGS) {
                int tpfFps = (int) (1.0 / Main.TPF);
                if (tpfFps < 60 && ((Main.FPS - tpfFps) > 30)) {
                    System.out.println("[Spike Lag Warning] From " + Main.FPS + " FPS to " + tpfFps + " FPS; current frame TPF: " + String.format("%.3f", Main.TPF) + "s");
                }
            }

            try {
                SwingUtilities.invokeAndWait(() -> {
                    BufferStrategy st = canvas.getBufferStrategy();
                    if (st == null || st.contentsLost()) {
                        canvas.createBufferStrategy(2);
                        return;
                    }
                    
                    if (CURSOR_HIDDEN && !HIDE_CURSOR) {
                        frame.setCursor(null);
                        CURSOR_HIDDEN = false;
                    }
                    if (!CURSOR_HIDDEN && HIDE_CURSOR) {
                        frame.setCursor(blankCursor);
                        CURSOR_HIDDEN = true;
                    }
                    
                    Runnable r;
                    while ((r = MAIN_TASKS.poll()) != null) {
                        r.run();
                    }

                    Graphics2D g = (Graphics2D) st.getDrawGraphics();

                    Game.get().loop(g);

                    st.show();
                    g.dispose();
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }

            frames++;
            if (System.currentTimeMillis() >= nextFpsUpdate) {
                Main.FPS = frames;
                frames = 0;
                nextFpsUpdate = System.currentTimeMillis() + 1000;
            }

            if (System.currentTimeMillis() >= nextTitleUpdate) {
                nextTitleUpdate = System.currentTimeMillis() + 100;
            }

            Main.ONE_SECOND_COUNTER += Main.TPF;
            Main.ONE_MINUTE_COUNTER += Main.TPF;

            if (Main.ONE_SECOND_COUNTER > 1.0) {
                Main.ONE_SECOND_COUNTER = 0.0;
            }
            if (Main.ONE_MINUTE_COUNTER > 60.0) {
                Main.ONE_MINUTE_COUNTER = 0.0;
            }

            Main.FRAME++;
        }
    }

}
