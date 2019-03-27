package io.paratek.rs.deob.impl;

import com.google.common.flogger.FluentLogger;
import io.paratek.rs.deob.InsnPattern;
import io.paratek.rs.deob.Transformer;
import io.paratek.rs.deob.asm.MethodNodeWrapper;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.*;

import java.util.ListIterator;
import java.util.Map;

public class IllegalStateExceptions extends Transformer {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private int removed = 0;

    @Override
    public void run(Map<String, ClassNode> classMap) {
        classMap.values().forEach(classNode -> classNode.methods.forEach(mn -> this.processMethod((MethodNode) mn)));
        logger.atInfo().log("Removed " + this.removed + " IllegalStateExceptions");
    }

    /**
     * Locate a new IllegalStateException, traverse backwards checking for an LDC or VarInsnNode for the last parameter
     *
     * @param methodNode
     */
    private void processMethod(final MethodNode methodNode) {
        final MethodNodeWrapper wrapper = new MethodNodeWrapper(methodNode);
        for (ListIterator it = wrapper.getMethodNode().instructions.iterator(); it.hasNext(); ) {
            AbstractInsnNode insnNode = (AbstractInsnNode) it.next();
            final boolean pattern = new InsnPattern(insnNode, node -> node instanceof VarInsnNode && ((VarInsnNode) node).var == wrapper.getOpaquePredicateVarIndex())
                    .next(node -> node instanceof LdcInsnNode || node instanceof IntInsnNode || (node.getOpcode() >= Opcodes.ICONST_M1 && node.getOpcode() <= Opcodes.ICONST_5))
                    .next(node -> node instanceof JumpInsnNode && node.getOpcode() != Opcodes.GOTO)
                    .next(node -> node.getOpcode() == Opcodes.NEW && ((TypeInsnNode) node).desc.equals("java/lang/IllegalStateException"))
                    .matches();
            if (pattern) {
                removed++;
                LabelNode labelNode = null;
                while (insnNode.getOpcode() != Opcodes.ATHROW) {
                    if (insnNode instanceof JumpInsnNode) {
                        labelNode = ((JumpInsnNode) insnNode).label;
                    }
                    it.remove();
                    insnNode = (AbstractInsnNode) it.next();
                }
                if (labelNode != null) {
                    it.previous();
                    it.add(new JumpInsnNode(Opcodes.GOTO, labelNode));
                }
            }
        }
    }

}
