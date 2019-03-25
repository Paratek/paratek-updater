package io.paratek.rs.deob.impl;

import io.paratek.rs.deob.InsnBlock;
import io.paratek.rs.deob.Transformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class ControlFlow extends Transformer {

    @Override
    public void run(Map<String, ClassNode> classMap) {
        classMap.values().forEach(classNode ->
                classNode.methods.forEach(mn -> this.accept((MethodNode) mn)));
    }

    private void accept(final MethodNode methodNode) {
        final List<InsnBlock> insnBlocks = new ArrayList<>();
        InsnBlock currentBlock = new InsnBlock();
        for (ListIterator it = methodNode.instructions.iterator(); it.hasNext();) {
            AbstractInsnNode node = (AbstractInsnNode) it.next();
            if (node.getOpcode() == Opcodes.GOTO) {
                final InsnBlock newBlock = new InsnBlock();
                newBlock.setParent(currentBlock);
                currentBlock.setChild(newBlock);
                currentBlock = newBlock;
            }
            currentBlock.addNode(node);
        }
    }

}
