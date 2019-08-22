package io.paratek.rs.deob.impl;

import com.google.common.flogger.FluentLogger;
import io.paratek.rs.deob.MethodBlock;
import io.paratek.rs.deob.Transformer;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.Map;

public class ControlFlow extends Transformer {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private int sortedMethods = 0;
    private int sortedBlocks = 0;
    private int removedblocks = 0;

    @Override
    public void run(Map<String, ClassNode> classMap) {

        classMap.values().forEach(classNode ->
                classNode.methods.stream()
                        .filter(methodNode -> methodNode.tryCatchBlocks.size() == 0)
                        .forEach(mn -> this.accept((MethodNode) mn)));
//
//        classMap.get("al").methods.stream()
//                .filter(methodNode -> methodNode.name.equals("w"))
//                .forEach(this::accept);

//        classMap.values().forEach(classNode -> classNode.methods.forEach(this::accept));

//        final MethodNode mn = classMap.get("client").methods.stream()
//                .filter(methodNode -> methodNode.tryCatchBlocks.size() == 0)
//                .findFirst().get();
//        this.accept(mn);

        logger.atInfo().log("Sorted " + this.sortedMethods + " methods");
        logger.atInfo().log("Sorted " + this.sortedBlocks + " blocks and removed " + this.removedblocks + " blocks");
    }

    private void accept(final MethodNode methodNode) {
        final MethodBlock methodBlock = new MethodBlock(methodNode);
        methodBlock.process();
        this.sortedBlocks += methodBlock.getSortedBlockCount();
        this.sortedMethods += 1;
        this.removedblocks += methodBlock.getRemovedBlockCount();
    }



}
