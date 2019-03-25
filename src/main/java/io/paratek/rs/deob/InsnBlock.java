package io.paratek.rs.deob;

import org.objectweb.asm.tree.AbstractInsnNode;

import java.util.LinkedList;

public class InsnBlock {

    private InsnBlock child, parent;
    private final LinkedList<AbstractInsnNode> insnNodes = new LinkedList<>();

    public void addNode(final AbstractInsnNode node) {
        this.insnNodes.addLast(node);
    }

    public void setChild(InsnBlock child) {
        this.child = child;
    }

    public void setParent(InsnBlock parent) {
        this.parent = parent;
    }

    public InsnBlock getChild() {
        return child;
    }

    public InsnBlock getParent() {
        return parent;
    }

    public LinkedList<AbstractInsnNode> getInsnNodes() {
        return insnNodes;
    }

}
