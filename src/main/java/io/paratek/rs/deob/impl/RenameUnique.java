package io.paratek.rs.deob.impl;

import io.paratek.rs.deob.Transformer;
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
            ClassNode copy = new ClassNode();

        }

    }

    private class NameGenerator {

        private int count = 0;
        private final String base;

        public NameGenerator(String base) {
            this.base = base;
        }

        public String next() {
            return base + count++;
        }

        public void reset() {
            this.count = 0;
        }

    }

}
