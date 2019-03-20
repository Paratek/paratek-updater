package io.paratek.rs.deob;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.Map;

public abstract class ChunkWorker implements Runnable {

    protected final int threadIndex;
    protected final Map<String, ClassNode> batch;

    public ChunkWorker(int threadIndex, Map<String, ClassNode> batch) {
        this.threadIndex = threadIndex;
        this.batch = batch;
    }

}
