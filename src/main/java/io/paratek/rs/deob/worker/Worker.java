package io.paratek.rs.deob.worker;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.Map;

public abstract class Worker implements Runnable {

    protected final int threadIndex;
    protected final Map<String, ClassNode> batch;

    public Worker(int threadIndex, Map<String, ClassNode> batch) {
        this.threadIndex = threadIndex;
        this.batch = batch;
    }

}
