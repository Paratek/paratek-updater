package io.paratek.rs.loader;

import com.mashape.unirest.http.Unirest;
import io.paratek.rs.analysis.hook.Game;
import io.paratek.rs.util.JarHandler;
import org.json.JSONObject;
import org.objectweb.asm.tree.ClassNode;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Frame extends JFrame {

    private final JPanel gamePanel = new JPanel();

    public Frame() {
        super("RuneScape");
        super.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        /* Setup GamePanel layout */
        gamePanel.setLayout(null);
        gamePanel.setSize(new Dimension(800, 600));
        gamePanel.setLocation(0, 0);
        gamePanel.setBackground(new Color(36, 41, 49));

        super.getContentPane().setLayout(null);
        super.getContentPane().add(gamePanel);
        super.getContentPane().setBackground(new Color(64, 72, 85));
        super.getContentPane().setPreferredSize(new Dimension(800, 600));
        super.pack();
    }

    public JPanel getGamePanel() {
        return gamePanel;
    }

    public static void start(final JarHandler jarHandler) {
        final Frame[] frame = new Frame[1];
        EventQueue.invokeLater(() -> {
            frame[0] = new Frame();
            frame[0].setLocationRelativeTo(null);
            frame[0].setVisible(true);
        });

        final ConfigLoader configLoader = new ConfigLoader(Game.OSRS);
        configLoader.load();

        try {
            final Map<String, ClassNode> classNodeHashMap = jarHandler.getClassMap();
            ClassNodeLoader classLoader = new ClassNodeLoader(classNodeHashMap);
            Class<?> clientClass = classLoader.loadClass(configLoader.getConfigs().get("initial_class").replace(".class", ""));
            Applet applet = (Applet) clientClass.newInstance();
            EventQueue.invokeLater(() -> {
                frame[0].getGamePanel().removeAll();
                frame[0].getGamePanel().add(applet);

                RSAppletStub appletStub = new RSAppletStub(configLoader.getConfigs());
                ((RSAppletContext) appletStub.getAppletContext()).setApplet(applet);
                applet.setStub(appletStub);
                applet.init();
                applet.setSize(800, 600);
                appletStub.setActive(true);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startRs3(final JarHandler jarHandler) {
        final Frame[] frame = new Frame[1];
        EventQueue.invokeLater(() -> {
            frame[0] = new Frame();
            frame[0].setLocationRelativeTo(null);
            frame[0].setVisible(true);
        });

        final ConfigLoader configLoader = new ConfigLoader(Game.RS3);
        configLoader.load();

        try {
            final Map<String, ClassNode> classNodeHashMap = jarHandler.getClassMap();
            final Rs2Applet applet = new Rs2Applet(classNodeHashMap);
            EventQueue.invokeLater(() -> {
                frame[0].getGamePanel().removeAll();
                frame[0].getGamePanel().add(applet);

                RSAppletStub appletStub = new RSAppletStub(configLoader.getConfigs());
                ((RSAppletContext) appletStub.getAppletContext()).setApplet(applet);
                applet.setStub(appletStub);
                applet.init();
                applet.setSize(800, 600);
                appletStub.setActive(true);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}