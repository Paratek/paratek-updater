package io.paratek.rs.deob;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MethodBlock {

    private final MethodNode methodNode;
    private final List<InsnBlock> rawBlockList = new ArrayList<>();
    private final List<InsnBlock> sortedBlockList = new ArrayList<>();
    private final Map<LabelNode, Integer> callCount = new HashMap<>();

    public MethodBlock(final MethodNode methodNode) {
        this.methodNode = methodNode;
    }


    /**
     * Run the order of operations to sort the instructions
     */
    public void process() {
        this.preprocess();
        this.parseAndChainBlocks();
        for (InsnBlock block : this.rawBlockList) {
            System.out.println(block);
        }
        System.out.println("YEET");
        this.linkBranchingBlocks();
        this.sortBlocks();
        InsnIterator.process(this.methodNode, node -> {
            System.out.println(node);
        });
    }

    public void preprocess() {
        InsnIterator.process(this.methodNode, node -> {
            if (node instanceof JumpInsnNode) {
                final LabelNode target = ((JumpInsnNode) node).label;
                if (this.callCount.containsKey(target)) {
                    this.callCount.put(target, this.callCount.get(target) + 1);
                } else {
                    this.callCount.put(target, 1);
                }
            }
        });
    }

//    /**
//     * Removes the exception blocks so that they can be added to the end of the MethodNode later
//     */
//    public void extractExceptionBlocks() {
//        if (this.methodNode.tryCatchBlocks.size() > 0) {
//            final Iterator<TryCatchBlockNode> trys = this.methodNode.tryCatchBlocks.iterator();
//            while (trys.hasNext()) {
//                final TryCatchBlockNode cur = trys.next();
//
//
//            }
//        }
//    }


    /**
     * Sorts the rawBlockList putting the sorted InsnBlocks into sortedBlockList
     */
    private void sortBlocks() {
        if (this.rawBlockList.size() > 0) {
            this.dfs(this.rawBlockList.get(0));
        }
        for (InsnBlock block : this.sortedBlockList) {
            System.out.println(block);
        }
        // Have to empty list with iterator otherwise it doesn't update correctly
        final Iterator<AbstractInsnNode> nodeIterator = this.methodNode.instructions.iterator();
        while (nodeIterator.hasNext()) {
            nodeIterator.next();
            nodeIterator.remove();
        }
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
        if (start.traversed || start.terminates) {
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

//            if (insnNode.getOpcode() == Opcodes.GOTO || (insnNode.getOpcode() >= 172 && insnNode.getOpcode() <= 177)) {
            if (insnNode instanceof JumpInsnNode || (insnNode.getOpcode() >= 172 && insnNode.getOpcode() <= 177)) {
                final InsnBlock tmp = new InsnBlock();
                rawBlockList.add(tmp);
                currentBlock[0] = tmp;
            }
        });
    }


    /**
     * Finds all of the child InsnBlocks for each InsnBlock in the rawBlockList
     */
    private void linkBranchingBlocks() {
        final ListIterator<InsnBlock> blockIterator = this.rawBlockList.listIterator();
        while (blockIterator.hasNext()) {
            final InsnBlock block = blockIterator.next();
            if (block.contents.size() > 0) {
                AbstractInsnNode last = block.contents.getLast();
                if (last instanceof JumpInsnNode) {
                    InsnBlock child = this.findLabelOwner(((JumpInsnNode) last).label);
                    if (child != null) {
                        if (last.getOpcode() != Opcodes.GOTO && blockIterator.hasNext()) {
                            final InsnBlock next = blockIterator.next();
                            block.children.add(next);
                            next.parents.add(block);
                            blockIterator.previous();
                        }
                        block.children.add(child);
                        child.parents.add(block);
                    }
                }
            }
        }
    }

    private void compressBlocks() {
        final ListIterator<AbstractInsnNode> nodeListIterator = this.methodNode.instructions.iterator();
        boolean shouldRemove = false;
        while (nodeListIterator.hasNext()) {
            AbstractInsnNode node = nodeListIterator.next();

            if (shouldRemove) {
                nodeListIterator.remove();
                if (node instanceof LabelNode) {
                    shouldRemove = false;
                }
            }

            if (node.getOpcode() == Opcodes.GOTO && !(node.getNext() instanceof LabelNode) && node.getNext() != null) {
                shouldRemove = true;
            }
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
