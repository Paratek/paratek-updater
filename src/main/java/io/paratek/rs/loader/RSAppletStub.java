package io.paratek.rs.loader;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Implementation of AppletStub to reload the game
 * Pulls data from Configs
 */
public class RSAppletStub implements AppletStub {

    private final Configs configs;
    private final RSAppletContext context;
    private boolean active = false;

    public RSAppletStub(Configs configs) {
        this.configs = configs;
        this.context = new RSAppletContext();
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public URL getDocumentBase() {
        try {
            return new URL(this.configs.get("codebase"));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Document Base URL");
        }
    }

    @Override
    public URL getCodeBase() {
        try {
            return new URL(this.configs.get("codebase"));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid Code Base URL");
        }
    }

    @Override
    public String getParameter(String name) {
        if (this.configs.containsKey(name)) {
            return this.configs.get(name);
        }
        return null;
    }

    @Override
    public AppletContext getAppletContext() {
        return this.context;
    }

    @Override
    public void appletResize(int width, int height) {
        final Applet applet = getAppletContext().getApplet("main");
        if (applet != null) {
            applet.resize(width, height);
        }
    }
}
