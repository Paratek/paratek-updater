package io.paratek.rs.deob.asm;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class MethodNodeWrapper {

    private final MethodNode methodNode;

    public MethodNodeWrapper(MethodNode methodNode) {
        this.methodNode = methodNode;
    }

    public List<String> getSplitDescription() {
        return ASM.extractDesc(this.methodNode);
    }

    public String getReturnType() {
        final List<String> desc = ASM.extractDesc(this.methodNode);
        if (desc.size() == 0) {
            return null;
        }
        return desc.get(desc.size() - 1);
    }

    public String getLastParameterType() {
        final List<String> desc = ASM.extractDesc(this.methodNode);
        if (desc.size() <= 1) {
            return null;
        }
        return desc.get(desc.size() - 2);
    }

    public int getLastParameterIndex() {
        final List<String> desc = ASM.extractDesc(this.methodNode);
        if (desc.size() <= 1) {
            return -1;
        }
        return desc.size() - 2;
    }

    public boolean isStatic() {
        return (this.methodNode.access & Opcodes.ACC_STATIC) != 0;
    }

    public int getOpaquePredicateVarIndex() {
        int last = this.getLastParameterIndex();
        return last == -1 ? -1 : this.isStatic() ? last : last + 1;
    }

    public String getOpaquePredicateType() {
        int index = this.getOpaquePredicateVarIndex();
        return index != -1 ? this.getLastParameterType() : null;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

}
