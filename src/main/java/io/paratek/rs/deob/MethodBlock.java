package io.paratek.rs.deob;

import jdk.internal.org.objectweb.asm.Label;
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
        this.parseAndChainBlocks();
        this.linkBranchingBlocks();
        this.sortBlocks();
//        this.postProcess();
    }


    /**
     *
     */
    public void postProcess() {
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
        final ListIterator<AbstractInsnNode> nodeListIterator = this.methodNode.instructions.iterator();
        while (nodeListIterator.hasNext()) {
            final AbstractInsnNode cur = nodeListIterator.next();
            if (cur.getOpcode() == Opcodes.GOTO && this.callCount.containsKey(((JumpInsnNode) cur).label)) {
                final int count = this.callCount.get(((JumpInsnNode) cur).label);
                if (count <= 1 && cur.getNext() instanceof LabelNode
                        && ((LabelNode) cur.getNext()).getLabel().equals(((JumpInsnNode) cur).label.getLabel())) {
                    nodeListIterator.remove();
                }
            }
        }
//        final List<LabelNode> currentLabels = new ArrayList<>();
//        InsnIterator.process(this.methodNode, node -> {
//            if (node instanceof LabelNode) {
//                currentLabels.add((LabelNode) node);
//            }
//        });
//         If we remove say the block at the end of the try catch, just before the handler, we have to update the catch end
//         Likewise we have to update the catch start
//        this.methodNode.tryCatchBlocks.removeIf(catchBlockNode -> !currentLabels.contains(catchBlockNode.handler));
    }


    /**
     * Sorts the rawBlockList putting the sorted InsnBlocks into sortedBlockList
     */
    private void sortBlocks() {
        if (this.rawBlockList.size() > 0) {
            this.dfs(this.rawBlockList.get(0));
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
        final TryCatchBlockNode[] current = {null};

        final InsnBlock[] currentBlock = {new InsnBlock()};
        this.rawBlockList.add(currentBlock[0]);

        InsnIterator.process(this.methodNode, insnNode -> {
            currentBlock[0].contents.add(insnNode);

            // See if we're in try catch block
            if (insnNode instanceof LabelNode) {
                if (current[0] == null) {
                    for (TryCatchBlockNode tryCatchBlockNode : this.methodNode.tryCatchBlocks) {
                        if (tryCatchBlockNode.start.equals(insnNode)) {
                            current[0] = tryCatchBlockNode;
                        }
                    }
                } else {
                    if (current[0].end.equals(insnNode)) {
                        current[0] = null;
                    }
                }
            }
            currentBlock[0].catchContainer = current[0];

            if (insnNode instanceof JumpInsnNode
                    || (insnNode.getOpcode() >= 172 && insnNode.getOpcode() <= 177)
                    || insnNode.getOpcode() == Opcodes.ATHROW
                    || insnNode instanceof TableSwitchInsnNode) {
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
                    } else {
                        throw new IllegalStateException("Could not find InsnBlock that owns " + ((JumpInsnNode) last).label);
                    }
                } else if (last.getOpcode() == Opcodes.ATHROW && block.catchContainer != null) {
                    final InsnBlock handler = this.findLabelOwner(block.catchContainer.handler);
                    if (handler != null) {
                        block.children.add(handler);
                        handler.parents.add(block);
                    } else {
                        throw new IllegalStateException("TryCatchBlockNode handler is null");
                    }
                } else if (last instanceof TableSwitchInsnNode) {
                    for (LabelNode labelNode : ((TableSwitchInsnNode) last).labels) {
                        final InsnBlock target = this.findLabelOwner(labelNode);
                        if (target != null) {
                            System.out.println("YEET");
                            block.children.add(target);
                            target.parents.add(block);
                        }
                    }
                }
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
