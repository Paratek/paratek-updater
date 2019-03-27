package io.paratek.rs.deob;

import jdk.internal.org.objectweb.asm.tree.AbstractInsnNode;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class InsnPattern {

    private final AbstractInsnNode startNode;
    private final List<Predicate<AbstractInsnNode>> predicates = new ArrayList<>();

    public InsnPattern(AbstractInsnNode startNode, Predicate<AbstractInsnNode> predicate) {
        this.startNode = startNode;
        this.predicates.add(predicate);
    }

    public InsnPattern next(final Predicate<AbstractInsnNode> predicate) {
        this.predicates.add(predicate);
        return this;
    }

    public boolean matches() {
        AbstractInsnNode cur = this.startNode;
        for (Predicate<AbstractInsnNode> nodePredicate : this.predicates) {
            if (cur != null && nodePredicate.test(cur)) {
                cur = cur.getNext();
            } else {
                return false;
            }
        }
        return true;
    }

}
