package io.paratek.rs.deob;

import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InsnBlock {

    public final LinkedList<AbstractInsnNode> contents = new LinkedList<>();
    public final List<InsnBlock> children = new ArrayList<>();
    public final List<InsnBlock> parents = new ArrayList<>();

    public boolean traversed = false;

}
