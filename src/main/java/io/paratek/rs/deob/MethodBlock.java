package io.paratek.rs.deob;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.JumpInsnNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class MethodBlock {

    private final MethodNode methodNode;
    private final List<InsnBlock> rawBlockList = new ArrayList<>();
    private final List<InsnBlock> sortedBlockList = new ArrayList<>();

    private final List<JumpInsnNode> jumpNodeWhiteList = new ArrayList<>();
    private final List<LabelNode> labelNodeWhiteList = new ArrayList<>();

    public MethodBlock(final MethodNode methodNode) {
        this.methodNode = methodNode;
    }

    public void process() {
        this.parseAndChainBlocks();
        this.linkBranchingBlocks();
    }

    private void parseAndChainBlocks() {
        final InsnBlock[] currentBlock = {new InsnBlock()};
        this.rawBlockList.add(currentBlock[0]);

        InsnIterator.process(this.methodNode, insnNode -> {
            currentBlock[0].contents.add(insnNode);

            if (insnNode.getOpcode() == Opcodes.GOTO) {
                final InsnBlock tmp = new InsnBlock();
                currentBlock[0].children.add(tmp);
                tmp.parents.add(currentBlock[0]);
                rawBlockList.add(tmp);
            } else {
                if (insnNode instanceof JumpInsnNode) {
                    this.jumpNodeWhiteList.add((JumpInsnNode) insnNode);
                    this.labelNodeWhiteList.add(((JumpInsnNode) insnNode).label);
                }
            }
        });
    }

    private void linkBranchingBlocks() {
        for (InsnBlock block : this.rawBlockList) {
            InsnIterator.process(block, insnNode -> {
                if (insnNode instanceof JumpInsnNode) {

                }
            });
        }
    }

}
