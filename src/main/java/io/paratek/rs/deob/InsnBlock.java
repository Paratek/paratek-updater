package io.paratek.rs.deob;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class InsnBlock {

    public final LinkedList<AbstractInsnNode> contents = new LinkedList<>();
    public final List<InsnBlock> children = new ArrayList<>();
    public final List<InsnBlock> parents = new ArrayList<>();

    public boolean traversed = false, terminates = false;

    public TryCatchBlockNode catchContainer = null;

    /**
     * Remove leading instructions that are unreachable
     */
    public void prune() {
        final Iterator<AbstractInsnNode> nodeIterator = this.contents.iterator();
        while (nodeIterator.hasNext() && !(nodeIterator.next() instanceof LabelNode)) {
            nodeIterator.remove();
        }
    }

    public String superString() {
        return super.toString();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("InsnBlock@" + super.toString() + " {\n");
        for (AbstractInsnNode node : this.contents) {
            str.append("    ").append(node.getOpcode()).append("     ").append(node.toString()).append("\n");
        }
        str.append("}, ").append("Children: ").append(this.children.size()).append("    ");
        for (InsnBlock children : this.children) {
            str.append(children.superString()).append(", ");
        }
        return str.toString();
    }
}
