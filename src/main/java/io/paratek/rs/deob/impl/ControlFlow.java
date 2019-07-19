package io.paratek.rs.deob.impl;

import io.paratek.rs.deob.Transformer;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.Map;

public class ControlFlow extends Transformer {

    @Override
    public void run(Map<String, ClassNode> classMap) {
//        classMap.values().forEach(classNode ->
//                classNode.methods.stream()
//                        .filter(methodNode -> methodNode.tryCatchBlocks.size() == 0)
//                        .forEach(mn -> this.accept((MethodNode) mn)));
        classMap.get("client").methods.stream()
                .filter(methodNode -> methodNode.tryCatchBlocks.size() == 0)
                .forEach(this::accept);
    }

    private void accept(final MethodNode methodNode) {

    }



}
