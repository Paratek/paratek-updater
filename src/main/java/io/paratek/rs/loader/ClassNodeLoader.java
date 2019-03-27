package io.paratek.rs.loader;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.Permissions;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Map;


/**
 * A ClassLoader that uses ClassNodes instead of Classes or URLClasses?
 * <p>
 * Not going to bother documenting each function, learn about ClassLoaders, functionality is obvious
 *
 * @author Parametric
 */
public class ClassNodeLoader extends ClassLoader {

    private final Map<String, ClassNode> classes;

    public ClassNodeLoader(final Map<String, ClassNode> classNodeHashMap) {
        this.classes = classNodeHashMap;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.findClass(name);
    }

    public Class<?> findClass(final String name) throws ClassNotFoundException {
        if (this.classes.containsKey(name)) {
            return this.nodeToClass(this.classes.get(name));
        }
        return super.loadClass(name);
    }

    private Class<?> nodeToClass(ClassNode node) {
        if (super.findLoadedClass(node.name.replace("/", ".")) != null) {
            return this.findLoadedClass(node.name.replace("/", "."));
        }
        final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        try {
            node.accept(cw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final byte[] b = cw.toByteArray();
        return this.defineClass(node.name.replaceAll("/", "."), b, 0, b.length, this.getDomain());
    }

    private ProtectionDomain getDomain() {
        CodeSource code = new CodeSource(null, (Certificate[]) null);
        return new ProtectionDomain(code, getPermissions());
    }

    private Permissions getPermissions() {
        Permissions permissions = new Permissions();
        permissions.add(new AllPermission());
        return permissions;
    }

}
