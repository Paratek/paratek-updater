package io.paratek.rs.deob.impl;

import com.google.common.flogger.FluentLogger;
import io.paratek.rs.deob.Transformer;
import io.paratek.rs.deob.asm.BetterRemapper;
import io.paratek.rs.util.BytecodeUtils;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.commons.RemappingClassAdapter;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;

public class RenameUnique extends Transformer {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private final List<String> whiteList = Arrays.asList("main",
            "supplyApplet", "init", "stop", "start", "update", "paint", "destroy", "offer");
    private final Map<String, Class<?>> unknownClasses = new HashMap<>();

    @Override
    public void run(Map<String, ClassNode> classMap) {
        String str = "";
        try {
            final List<String>  lines = Files.readAllLines(Paths.get("/home/sysassist/Desktop/179/generated-resources/hooks.json"));
            final StringBuilder builder = new StringBuilder();
            lines.forEach(builder::append);
            str = builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        final JSONArray hooks = new JSONArray(str);

        // Create our mappings
        final NameGenerator classNameGenerator = new NameGenerator("class");
        final NameGenerator methodNameGenerator = new NameGenerator("method");
        final NameGenerator fieldNameGenerator = new NameGenerator("field");

        final Map<String, String> classNameMappings = new HashMap<>();
        final Map<String, String> fieldNameMappings = new HashMap<>();
        final Map<String, String> methodNameMappings = new HashMap<>();

        final List<MethodNode> methodNodes = new ArrayList<>();
        logger.atInfo().log("Generating unique name mappings");
        for (ClassNode classNode : classMap.values()) {
            // Classes
            // TODO:
            // Need to find a more universal way to not rename stuff that is called from native methods
            if (classNode.name.equals("client")) {
                classNameMappings.put(classNode.name, classNode.name);
            } else {
                for (int i = 0; i < hooks.length(); i++) {
                    final JSONObject object = hooks.getJSONObject(i);
                    final String name = object.getString("name");
                    if (name.equals(classNode.name)) {
                        classNameMappings.put(classNode.name, object.getString("class"));
                        logger.atInfo().log("Mapping class " + classNode.name + " to " + object.getString("class"));
                    }
                }
            }
            // Fields
            for (FieldNode fieldNode : classNode.fields) {
                final Stack<ClassNode> classNodeStack = new Stack<>();
                classNodeStack.push(classNode);
                while (classNodeStack.size() > 0) {
                    ClassNode owner = classNodeStack.pop();
                    for (int i = 0; i < hooks.length(); i++) {
                        final JSONObject object = hooks.getJSONObject(i);
                        final String name = object.getString("name");
                        if (name.equals(owner.name)) {
                            final JSONArray methods = object.getJSONArray("fields");
                            for (int j = 0; j < methods.length(); j++) {
                                final JSONObject method = methods.getJSONObject(j);
                                if (method.getString("name").equals(fieldNode.name) && method.getString("descriptor").equals(fieldNode.desc)) {
                                    fieldNameMappings.put(owner.name + "." + fieldNode.name + "-" + fieldNode.desc, method.getString("field"));
                                    System.out.println("Mapping " + owner.name + "." + fieldNode.name + fieldNode.desc + " to " + method.getString("field"));
                                }
                            }
                        }
                    }

                    for (ClassNode supercn : classMap.values()) {
                        if (supercn.superName.equals(owner.name)) {
                            classNodeStack.push(supercn);
                        }
                    }
                }
            }
            // Methods
            methodNodes.addAll(classNode.methods);
        }

        methods:
        for (MethodNode m : methodNodes) {
            ClassNode owner = this.getOwner(classMap, m);
            if (this.notLocal(classMap, owner, m)) {
                continue;
            }

            Stack<ClassNode> stack = new Stack<>();
            stack.push(owner);
            while (stack.size() > 0) {
                ClassNode node = stack.pop();
                if (node != owner && this.getMethod(node, m.name, m.desc) != null) {
                    continue methods;
                }
                ClassNode parent = classMap.get(node.superName);
                if (parent != null) {
                    stack.push(parent);
                }
                for (String itf : node.interfaces) {
                    ClassNode itfNode = classMap.get(itf);
                    if (itfNode != null) {
                        stack.push(itfNode);
                    }
                }
            }
//            String name = methodNameGenerator.next();
            stack.push(owner);
            while (stack.size() > 0) {
                ClassNode node = stack.pop();
                for (int i = 0; i < hooks.length(); i++) {
                    final JSONObject object = hooks.getJSONObject(i);
                    final String name = object.getString("name");
                    if (name.equals(node.name)) {
                        final JSONArray methods = object.getJSONArray("methods");
                        for (int j = 0; j < methods.length(); j++) {
                            final JSONObject method = methods.getJSONObject(j);
                            if (method.getString("name").equals(m.name) && method.getString("descriptor").equals(m.desc)) {
                                methodNameMappings.put(node.name + "." + m.name + m.desc, method.getString("method"));
                                System.out.println("Mapping " + node.name + "." + m.name + m.desc + " to " + method.getString("method"));
                            }
                        }
                    }
                }
                classMap.values().forEach(classNode -> {
                    if (classNode.superName.equals(node.name) || classNode.interfaces.contains(node.name)) {
                        stack.push(classNode);
                    }
                });
            }
        }
        // Do all of our modifications
        logger.atInfo().log("Applying unique name mappings");
        final BetterRemapper simpleClassRemapper = new BetterRemapper(classNameMappings);
        final BetterRemapper simpleFieldRemapper = new BetterRemapper(fieldNameMappings);
        final BetterRemapper simpleMethodRemapper = new BetterRemapper(methodNameMappings);

        this.applyMappings(classMap, simpleFieldRemapper);
        this.applyMappings(classMap, simpleMethodRemapper);
        this.applyMappings(classMap, simpleClassRemapper);

        logger.atInfo().log("Renamed " + classNameGenerator.getCount() + " classes");
        logger.atInfo().log("Renamed " + methodNameGenerator.getCount() + " methods");
        logger.atInfo().log("Renamed " + fieldNameGenerator.getCount() + " fields");
    }

    private boolean notLocal(Map<String, ClassNode> classMap, ClassNode classNode, MethodNode methodNode) {
        return this.whiteList.contains(methodNode.name) || methodNode.name.contains("<")
                || (methodNode.access & Opcodes.ACC_NATIVE) != 0
                || !this.isLocalMethod(classMap, classNode, methodNode.name);
    }

    private MethodNode getMethod(ClassNode node, String name, String desc) {
        for (MethodNode methodNode : (List<MethodNode>) node.methods) {
            if (methodNode.name.equals(name) && methodNode.desc.equals(desc)) {
                return methodNode;
            }
        }
        return null;
    }

    private ClassNode getOwner(final Map<String, ClassNode> classMap, final MethodNode methodNode) {
        return this.findFirst(classMap.values(), classNode -> classNode.methods.contains(methodNode));
    }

    private <T> T findFirst(Collection<T> collection, Predicate<T> predicate) {
        for (T t : collection) {
            if (predicate.test(t)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Check if a method name belongs to a class with reflection
     *
     * @param clazz
     * @param methodName
     * @return
     */
    private boolean checkClass(final Class<?> clazz, final String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName.replace("/", "."))) {
                return true;
            }
        }
        return false;
    }


    /**
     * Uses reflection to lookup a class name
     *
     * @param name
     * @return
     */
    private Class<?> findClass(final String name) {
        try {
            return this.getClass().getClassLoader().loadClass(name.replace("/", "."));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Checks if a Method belongs to the code that's being deobfuscated and is not a native class
     *
     * @param classMap
     * @param classNode
     * @param methodName
     * @return
     */
    private boolean isLocalMethod(final Map<String, ClassNode> classMap, final ClassNode classNode, final String methodName) {
        final Stack<ClassNode> stack = new Stack<>();
        stack.push(classNode);
        while (stack.size() > 0) {
            final ClassNode curr = stack.pop();
            final ClassNode superNode = this.getFromName(classMap, curr.superName);
            if (superNode != null) {
                stack.push(superNode);
            } else {
                if (this.unknownClasses.containsKey(curr.superName)) {
                    if (this.checkClass(this.unknownClasses.get(curr.superName), methodName)) {
                        return false;
                    }
                } else {
                    final Class<?> clazz = this.findClass(curr.superName);
                    if (clazz != null && this.checkClass(clazz, methodName)) {
                        return false;
                    }
                }
            }
            for (String itf : (List<String>) curr.interfaces) {
                final ClassNode itfNode = this.getFromName(classMap, itf);
                if (itfNode != null) {
                    stack.push(itfNode);
                } else {
                    if (this.unknownClasses.containsKey(itf)) {
                        if (this.checkClass(this.unknownClasses.get(itf), methodName)) {
                            return false;
                        }
                    } else {
                        final Class<?> clazz = this.findClass(itf);
                        if (clazz != null && this.checkClass(clazz, methodName)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private ClassNode getFromName(final Map<String, ClassNode> classMap, String name) {
        name = name.replace("/", ".");
        if (classMap.containsKey(name)) {
            return classMap.get(name);
        }
        return null;
    }

    private void applyMappings(final Map<String, ClassNode> classMap, final BetterRemapper remapper) {
        final Map<String, ClassNode> copyMap = new HashMap<>();
        for (ClassNode classNode : new ArrayList<>(classMap.values())) {
            ClassReader classReader = new ClassReader(BytecodeUtils.getClassNodeBytes(classNode));
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            RemappingClassAdapter remappingClassAdapter = new RemappingClassAdapter(classWriter, remapper);
            classReader.accept(remappingClassAdapter, ClassReader.EXPAND_FRAMES);
            classReader = new ClassReader(classWriter.toByteArray());
            ClassNode newNode = new ClassNode();
            classReader.accept(newNode, 0);
            copyMap.put(classNode.name, newNode);
        }

        classMap.clear();
        classMap.putAll(copyMap);
    }

    private class NameGenerator {

        private final String base;
        private int count = 0;

        NameGenerator(String base) {
            this.base = base;
        }

        String next() {
            return base + count++;
        }

        String last() {
            return this.base + (this.count - 1);
        }

        void reset() {
            this.count = 0;
        }

        public int getCount() {
            return count;
        }
    }

}
