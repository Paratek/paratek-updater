package io.paratek.rs.deob.impl;

import io.paratek.rs.deob.Transformer;
import io.paratek.rs.util.BytecodeUtils;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.commons.RemappingClassAdapter;
import jdk.internal.org.objectweb.asm.commons.SimpleRemapper;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.Map;

public class RenameUnique extends Transformer {

    @Override
    public void run(Map<String, ClassNode> classMap) {
        final NameGenerator classNameGenerator = new NameGenerator("class");
        final NameGenerator methodNameGenerator = new NameGenerator("method");
        final NameGenerator fieldNameGenerator = new NameGenerator("field");

        final Map<String, String> classNameMappings = new HashMap<>();
        final Map<String, String> fieldNameMappings = new HashMap<>();
        final Map<String, String> methodNameMappings = new HashMap<>();
        for (ClassNode classNode : classMap.values()) {
            classNameMappings.put(classNode.name, classNameGenerator.next());
        }

        final SimpleRemapper simpleClassRemapper = new SimpleRemapper(classNameMappings);
        for (ClassNode classNode : classMap.values()) {
            ClassReader classReader = new ClassReader(BytecodeUtils.getClassNodeBytes(classNode));
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            RemappingClassAdapter remappingClassAdapter = new RemappingClassAdapter(classWriter, simpleClassRemapper);
            classReader.accept(remappingClassAdapter, ClassReader.EXPAND_FRAMES);
            classReader = new ClassReader(classWriter.toByteArray());
            ClassNode newNode = new ClassNode();
            classReader.accept(newNode, 0);

            classMap.remove(classNode.name);
            classMap.put(newNode.name, newNode);
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
