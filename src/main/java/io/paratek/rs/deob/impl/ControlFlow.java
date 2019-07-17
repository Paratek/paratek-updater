package io.paratek.rs.deob.impl;

import io.paratek.rs.deob.InsnBlock;
import io.paratek.rs.deob.Transformer;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.*;

public class ControlFlow extends Transformer {

    private int index = 0;
    private Stack<InsnBlock> blockStack = new Stack<>();

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

    private List<InsnBlock> buildBlocks(final MethodNode methodNode) {
        final List<InsnBlock> insnBlocks = new ArrayList<>();
        InsnBlock currentBlock = new InsnBlock();
        for (ListIterator it = methodNode.instructions.iterator(); it.hasNext(); ) {
            AbstractInsnNode node = (AbstractInsnNode) it.next();
            currentBlock.addNode(node);
            if (node.getOpcode() == Opcodes.GOTO) {
                final InsnBlock newBlock = new InsnBlock();
                newBlock.setParent(currentBlock);
                currentBlock.setChild(newBlock);
                insnBlocks.add(currentBlock);
                currentBlock = newBlock;
            }
        }
        for (InsnBlock block : insnBlocks) {
            if (block.getGotoBlock() == null) {
                final LabelNode target = block.getEndingLabelNode();
                if (target != null) {
                    for (InsnBlock test : insnBlocks) {
                        final AbstractInsnNode startNode = test.getInsnNodes().getFirst();
                        if (startNode instanceof LabelNode && startNode.equals(target)) {
                            block.setGotoBlock(test);
                        }
                    }
                }
            }
        }
        return insnBlocks;
    }

    private List<InsnBlock> tarjanConnect(InsnBlock block) {
        block.setIndex(this.index);
        block.setLowlink(this.index++);

    }

    private void accept(final MethodNode methodNode) {
        final List<InsnBlock> insnBlocks = this.buildBlocks(methodNode);
        if (insnBlocks.size() > 0) {
            for (InsnBlock b : insnBlocks) {
                if (b.getIndex() == -1) {
                    this.tarjanConnect(b);
                }
            }
//            methodNode.instructions = list;

//            System.out.println(list.size());
//            for (ListIterator<AbstractInsnNode> it = list.iterator(); it.hasNext(); ) {
//                AbstractInsnNode insn = it.next();
//                boolean insnPattern = new InsnPattern(insn, node -> node.getOpcode() == Opcodes.GOTO && node.getNext() != null)
//                        .next(node -> node instanceof LabelNode && ((JumpInsnNode) node.getPrevious()).label.equals(node))
//                        .matches();
//            }
        }
    }

    private void dfs(final InsnList list, final InsnBlock block) {
        if (!block.isTraversed()) {
            block.setTraversed(true);
            for (AbstractInsnNode node : block.getInsnNodes()) {
                list.add(node);
            }
            if (block.getGotoBlock() != null) {
                this.dfs(list, block.getGotoBlock());
            }
            if (block.getChild() != null) {
                this.dfs(list, block.getChild());
            }
        }
    }


}
