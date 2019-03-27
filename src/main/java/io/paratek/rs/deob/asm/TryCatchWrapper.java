package io.paratek.rs.deob.asm;

import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;
import jdk.internal.org.objectweb.asm.tree.LdcInsnNode;
import jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode;

public class TryCatchWrapper {

    private final TryCatchBlockNode node;
    private final MethodNodeWrapper mn;

    public TryCatchWrapper(TryCatchBlockNode node, final MethodNodeWrapper mn) {
        this.node = node;
        this.mn = mn;
    }

    public boolean isRemovableRuntime() {
        if (this.node.type != null && this.node.type.equals("java/lang/RuntimeException")) {
            AbstractInsnNode cur = this.node.start;
            while (cur != null) {
                if (cur instanceof LdcInsnNode) {
                    return true;
                }
                cur = cur.getNext();
            }
        }
        return false;
    }

}
