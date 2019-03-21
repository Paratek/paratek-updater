package io.paratek.rs.loader;

import io.paratek.rs.analysis.hook.Game;
import io.paratek.rs.util.JarHandler;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;

public class Frame extends JFrame {

    private final JPanel gamePanel = new JPanel();

    public Frame() {
        super.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.gamePanel.setLayout(null);
        this.gamePanel.setSize(new Dimension(765, 503));
        this.gamePanel.setLocation(0, 0);
        this.gamePanel.setBackground(new Color(36, 41, 49));
        super.getContentPane().setPreferredSize(new Dimension(765, 503));
        super.pack();
    }

    public JPanel getGamePanel() {
        return gamePanel;
    }

    public static void start(final JarHandler jarHandler) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        final Frame frame = new Frame();
        frame.setTitle("RuneScape");
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        final ConfigLoader configLoader = new ConfigLoader(jarHandler.getGame());
        configLoader.load();
        final ClassNodeLoader classLoader = new ClassNodeLoader(jarHandler.getClassMap());
        Class<?> clientClass = classLoader.loadClass("client");
        Applet applet = (Applet) clientClass.newInstance();

        EventQueue.invokeLater(() -> {
            frame.getGamePanel().removeAll();
            frame.getGamePanel().add(applet);

            RSAppletStub appletStub = new RSAppletStub(configLoader.getConfigs());
            ((RSAppletContext) appletStub.getAppletContext()).setApplet(applet);
            applet.setStub(appletStub);
            applet.init();
            applet.setSize(765, 503);
            appletStub.setActive(true);
        });

    }

}
