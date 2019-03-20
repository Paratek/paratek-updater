package io.paratek.rs.deob;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.Map;

public abstract class Transformer {

    public abstract void run(Map<String, ClassNode> classMap);

}
