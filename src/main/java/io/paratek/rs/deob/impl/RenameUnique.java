package io.paratek.rs.deob.impl;

import io.paratek.rs.deob.Transformer;
import io.paratek.rs.util.BytecodeUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.function.Predicate;

public class RenameUnique extends Transformer {

    private final List<String> whiteList = Collections.singletonList("main");

    @Override
    public void run(Map<String, ClassNode> classMap) {
        // Create our mappings
        final List<MethodNode> methodNodes = new ArrayList<>();

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
            //Fields
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
            methodNodes.addAll(classNode.methods);
        }

        // Do all of our modifications
        final HashMap<String, ClassNode> modifiedNodeMap = new HashMap<>();

        final SimpleRemapper simpleClassRemapper = new SimpleRemapper(classNameMappings);
        final SimpleRemapper simpleFieldRemapper = new SimpleRemapper(fieldNameMappings);
        final SimpleRemapper simpleMethodRemapper = new SimpleRemapper(methodNameMappings);

        this.applyMappings(classMap, simpleFieldRemapper);
        this.applyMappings(classMap, simpleMethodRemapper);
        this.applyMappings(classMap, simpleClassRemapper);

//        for (Map.Entry<String, ClassNode> pair : classMap.entrySet()) {
//            System.out.println(pair.getKey() + " -> " + pair.getValue().name);
//        }

        // Write everything back to the original HashMap
//        for (Map.Entry<String, ClassNode> item : modifiedNodeMap.entrySet()) {
//            classMap.put(item.getKey(), item.getValue());
//        }
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
