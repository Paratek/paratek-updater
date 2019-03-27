package io.paratek.rs.loader;

import javax.imageio.ImageIO;
import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;


/**
 * Implementation of AppletContext to reload the game
 */
public class RSAppletContext implements AppletContext {

    private final Map<String, InputStream> streams = new HashMap<>();
    private Applet applet;

    public AudioClip getAudioClip(URL url) {
        return Applet.newAudioClip(url);
    }

    public Image getImage(URL url) {
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    public Applet getApplet(String name) {
        return this.applet;
    }

    public Enumeration<Applet> getApplets() {
        Vector<Applet> applets = new Vector<>();
        applets.add(applet);
        return applets.elements();
    }

    public void showDocument(URL url) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(url.toURI());
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException("Unable to open document " + url.getPath());
            }
        }
    }

    public void showDocument(URL url, String target) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(url.toURI());
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException("Unable to open document " + url.getPath());
            }
        }
    }

    @Override
    public void showStatus(String status) {
    }

    @Override
    public void setStream(String key, InputStream stream) throws IOException {
        streams.remove(key);
        streams.put(key, stream);
    }

    @Override
    public InputStream getStream(String key) {
        return streams.get(key);
    }

    @Override
    public Iterator<String> getStreamKeys() {
        return streams.keySet().iterator();
    }

    public void setApplet(Applet applet) {
        this.applet = applet;
    }

}
