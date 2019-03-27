package io.paratek.rs.deob;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.JumpInsnNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;

import java.util.LinkedList;

public class InsnBlock {

    private final LinkedList<AbstractInsnNode> insnNodes = new LinkedList<>();
    private InsnBlock child, parent, gotoBlock;
    private boolean traversed = false;

    public void addNode(final AbstractInsnNode node) {
        this.insnNodes.addLast(node);
    }

    public InsnBlock getChild() {
        return child;
    }

    public void setChild(InsnBlock child) {
        this.child = child;
    }

    public InsnBlock getParent() {
        return parent;
    }

    public void setParent(InsnBlock parent) {
        this.parent = parent;
    }

    public void setGotoBlock(InsnBlock gotoBlock) {
        this.gotoBlock = gotoBlock;
    }

    public InsnBlock getGotoBlock() {
        return gotoBlock;
    }

    public LabelNode getEndingLabelNode() {
        if (this.insnNodes.getLast().getOpcode() == Opcodes.GOTO) {
            return ((JumpInsnNode) this.insnNodes.getLast()).label;
        }
        return null;
    }

    public boolean isTraversed() {
        return traversed;
    }

    public void setTraversed(boolean traversed) {
        this.traversed = traversed;
    }

    public LinkedList<AbstractInsnNode> getInsnNodes() {
        return insnNodes;
    }

}
