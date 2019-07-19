package io.paratek.rs.deob;

import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;
import java.util.function.Consumer;

public class InsnIterator {

    public static void process(final MethodNode methodNode, final Consumer<AbstractInsnNode> nodeConsumer) {
        final ListIterator<AbstractInsnNode> nodeListIterator = methodNode.instructions.iterator();
        while (nodeListIterator.hasNext()) {
            nodeConsumer.accept(nodeListIterator.next());
        }
    }

    public static void process(final InsnBlock insnBlock, final Consumer<AbstractInsnNode> nodeConsumer) {
        for (AbstractInsnNode node : insnBlock.contents) {
            nodeConsumer.accept(node);
        }
    }

}
