package io.paratek.rs.deob.impl;

import io.paratek.rs.deob.Transformer;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.Map;

public class OpaquePredicates extends Transformer {

    // Check if param is used, if not change the description and then find all MethodInsnNodes and their descriptions

    @Override
    public void run(Map<String, ClassNode> classMap) {

    }

}
