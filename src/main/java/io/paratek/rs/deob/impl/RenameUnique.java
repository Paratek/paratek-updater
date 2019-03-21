package io.paratek.rs.deob.impl;

import io.paratek.rs.deob.Transformer;
import io.paratek.rs.deob.annotations.Metadata;
import io.paratek.rs.util.BytecodeUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.*;

public class RenameUnique extends Transformer {

    private final List<String> whiteList = Collections.singletonList("main");
    private final Map<String, Class<?>> unknownClasses = new HashMap<>();

    @Override
    public void run(Map<String, ClassNode> classMap) {
        // Create our mappings
        final NameGenerator classNameGenerator = new NameGenerator("class");
        final NameGenerator methodNameGenerator = new NameGenerator("method");
        final NameGenerator fieldNameGenerator = new NameGenerator("field");

        final Map<String, String> classNameMappings = new HashMap<>();
        final Map<String, String> fieldNameMappings = new HashMap<>();
        final Map<String, String> methodNameMappings = new HashMap<>();
        for (ClassNode classNode : classMap.values()) {
            // Classes
            if (classNode.name.equals("client")) {
                classNameMappings.put(classNode.name, "Client");
            } else {
                classNameMappings.put(classNode.name, classNameGenerator.next());
            }
            // Fields
            for (FieldNode fieldNode : (List<FieldNode>) classNode.fields) {
                final Stack<ClassNode> classNodeStack = new Stack<>();
                classNodeStack.push(classNode);
                while (classNodeStack.size() > 0) {
                    ClassNode owner = classNodeStack.pop();
                    fieldNameMappings.put(owner.name + "." + fieldNode.name, fieldNameGenerator.next());
                    for (ClassNode supercn : classMap.values()) {
                        if (supercn.superName.equals(owner.name)) {
                            classNodeStack.push(supercn);
                        }
                    }
                }
            }
            // Methods
            for (MethodNode methodNode : (List<MethodNode>) classNode.methods) {
                if (this.whiteList.contains(methodNode.name) || methodNode.name.contains("<")
                        || (methodNode.access & Opcodes.ACC_NATIVE) != 0
                        || !this.isLocalMethod(classMap, classNode, methodNode.name, methodNode.desc)) {
                    continue;
                }
                // Check if method is overridden / gotten from interface or super class

            }
        }

        // Do all of our modifications
        final HashMap<String, ClassNode> modifiedNodeMap = new HashMap<>();

        final SimpleRemapper simpleClassRemapper = new SimpleRemapper(classNameMappings);
        final SimpleRemapper simpleFieldRemapper = new SimpleRemapper(fieldNameMappings);
        final SimpleRemapper simpleMethodRemapper = new SimpleRemapper(methodNameMappings);

        this.applyMappings(classMap, simpleFieldRemapper);
        this.applyMappings(classMap, simpleMethodRemapper);
//        this.applyMappings(classMap, simpleClassRemapper);

//        for (Map.Entry<String, ClassNode> pair : classMap.entrySet()) {
//            System.out.println(pair.getKey() + " -> " + pair.getValue().name);
//        }

        // Write everything back to the original HashMap
//        for (Map.Entry<String, ClassNode> item : modifiedNodeMap.entrySet()) {
//            classMap.put(item.getKey(), item.getValue());
//        }
    }


    /**
     * Check if a method name belongs to a class with reflection
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
     * @param classMap
     * @param classNode
     * @param methodName
     * @param methodDesc
     * @return
     */
    private boolean isLocalMethod(final Map<String, ClassNode> classMap, final ClassNode classNode, final String methodName, final String methodDesc) {
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

    private ClassNode getFromName(final Map<String, ClassNode> classMap, final String name) {
        if (classMap.containsKey(name)) {
            return classMap.get(name);
        }
        return null;
    }

    private void applyMappings(final Map<String, ClassNode> classMap, final SimpleRemapper remapper) {
        for (ClassNode classNode : new ArrayList<>(classMap.values())) {
            ClassReader classReader = new ClassReader(BytecodeUtils.getClassNodeBytes(classNode));
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            ClassRemapper remappingClassAdapter = new ClassRemapper(classWriter, remapper);
            classReader.accept(remappingClassAdapter, ClassReader.EXPAND_FRAMES);
            classReader = new ClassReader(classWriter.toByteArray());
            ClassNode newNode = new ClassNode();
            classReader.accept(newNode, 0);
            classMap.put(classNode.name, newNode);
        }
    }

    private class NameGenerator {

        private int count = 0;
        private final String base;

        NameGenerator(String base) {
            this.base = base;
        }

        String next() {
            return base + count++;
        }

        void reset() {
            this.count = 0;
        }

    }

}
