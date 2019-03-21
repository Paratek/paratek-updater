package io.paratek.rs.loader;

import org.objectweb.asm.tree.ClassNode;

import java.applet.Applet;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

public final class Rs2Applet extends Applet {

    private Object mainClassInstance = null;
    private Class<?> mainClass = null;

    private final Map<String, ClassNode> classMap;

    public Rs2Applet(final Map<String, ClassNode> classMap){
        this.classMap = classMap;
    }

    public final void destroy() {
        if (this.mainClassInstance != null) {
            this.invokeMethod(null, null, "destroy");
        }
    }

    private void invokeMethod(final Object[] arg0, final Class<?>[] arg2, final String arg3) {
        try {
            final Method method = this.mainClass.getMethod(arg3, arg2);
            method.invoke(this.mainClassInstance, arg0);
        } catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException var6) {
            var6.printStackTrace();
        }
    }

    public final void update(final Graphics2D arg0) {
        if (this.mainClassInstance != null) {
            this.invokeMethod(new Object[]{arg0}, new Class[]{Graphics2D.class}, "update");
        }
    }

    private void throwException(final Throwable arg1) {
        System.out.print("Client error called");
        arg1.printStackTrace();
    }

    public final void stop() {
        if (this.mainClassInstance != null) {
            this.invokeMethod(null, null, "stop");
        }
    }

    public final void start() {
        if (this.mainClassInstance != null) {
            this.invokeMethod(null, null, "start");
        }
    }

    public final void paint(final Graphics2D arg0) {
        if (this.mainClassInstance != null) {
            this.invokeMethod(new Object[]{arg0}, new Class[]{Graphics2D.class}, "paint");
        }
    }

    public final void init() {
        try {
            final ClassNodeLoader classNodeLoader = new ClassNodeLoader(this.classMap);
            this.mainClass = classNodeLoader.loadClass("client");
        } catch (final Exception e) {
            e.printStackTrace();
        }
        try {
            final Constructor<?> constructor = this.mainClass.getConstructor((Class[]) null);
            this.mainClassInstance = constructor.newInstance((Object[]) null);
        } catch (final NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException var25) {
            this.throwException(var25);
        }
        this.invokeMethod(new Object[]{this}, new Class[]{Applet.class}, "supplyApplet");
        this.invokeMethod(null, null, "init");
    }
}
