package io.paratek.rs.deob;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.InsnList;
import jdk.internal.org.objectweb.asm.tree.JumpInsnNode;
import jdk.internal.org.objectweb.asm.tree.LabelNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MethodBlock {

    private final MethodNode methodNode;
    private final List<InsnBlock> rawBlockList = new ArrayList<>();
    private final List<InsnBlock> sortedBlockList = new ArrayList<>();

    private final List<JumpInsnNode> jumpNodeWhiteList = new ArrayList<>();
    private final List<LabelNode> labelNodeWhiteList = new ArrayList<>();

    public MethodBlock(final MethodNode methodNode) {
        this.methodNode = methodNode;
    }


    /**
     * Run the order of operations to sort the instructions
     */
    public void process() {
        this.parseAndChainBlocks();
        this.linkBranchingBlocks();
        this.sortBlocks();
    }


    /**
     * Sorts the rawBlockList putting the sorted InsnBlocks into sortedBlockList
     */
    private void sortBlocks() {
        if (this.rawBlockList.size() > 0) {
            this.dfs(this.rawBlockList.get(0));
        }
        this.methodNode.instructions.clear();
        final InsnList list = new InsnList();
        for (InsnBlock block : this.sortedBlockList) {
            block.contents.forEach(list::add);
        }
        this.methodNode.instructions.add(list);
    }

    /**
     * Traverses the rawBlockList and puts the traversed blocks into the sortedBlockList
     * @param start the starting block to sort from
     */
    private void dfs(InsnBlock start) {
        if (start.traversed) {
            return;
        }
        start.traversed = true;
        this.sortedBlockList.add(start);
        for (InsnBlock child : start.children) {
            this.dfs(child);
        }
    }


    /**
     * Traverses the MethodNode until it reaches a GOTO instruction. When it finds a GOTO it ends the current InsnBlock,
     * creates a new InsnBlock and chains/links the two blocks.
     */
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


    /**
     * Finds all of the child InsnBlocks for each InsnBlock in the rawBlockList
     */
    private void linkBranchingBlocks() {
        for (InsnBlock block : this.rawBlockList) {
            InsnIterator.process(block, insnNode -> {
                if (insnNode instanceof JumpInsnNode) {
                    InsnBlock owner = this.findLabelOwner(((JumpInsnNode) insnNode).label);
                    if (owner != null) {
                        block.children.add(owner);
                        owner.parents.add(block);
                    }
                }
            });
        }
    }

    /**
     * Finds the InsnBlock that contains a the desired LabelNode
     * @param labelNode the LabelNode to find
     * @return the InsnBlock that owns the LabelNode
     */
    private InsnBlock findLabelOwner(final LabelNode labelNode) {
        AtomicBoolean found = new AtomicBoolean(false);
        for (InsnBlock block : this.rawBlockList) {
            InsnIterator.process(block, insnNode -> {
                if (insnNode instanceof LabelNode && labelNode.getLabel().equals(((LabelNode) insnNode).getLabel())) {
                    found.set(true);
                }
            });
            if (found.get()) {
                return block;
            }
        }
        return null;
    }

    /**
     *
     * @return the number of blocks sorted
     */
    public int getSortedBlockCount() {
        return this.sortedBlockList.size();
    }

    /**
     *
     * @return the number of blocks removed
     */
    public int getRemovedBlockCount() {
        return this.rawBlockList.size() - this.sortedBlockList.size();
    }

}
