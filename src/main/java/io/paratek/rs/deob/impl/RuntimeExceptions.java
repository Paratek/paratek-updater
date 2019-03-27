package io.paratek.rs.deob.impl;

import com.google.common.flogger.FluentLogger;
import io.paratek.rs.deob.Transformer;
import io.paratek.rs.deob.asm.MethodNodeWrapper;
import io.paratek.rs.deob.asm.TryCatchWrapper;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.Map;

public class RuntimeExceptions extends Transformer {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    private int removed = 0;

    @Override
    public void run(Map<String, ClassNode> classMap) {
        classMap.values().forEach(classNode -> classNode.methods.forEach(mn -> {
            MethodNodeWrapper wrapper = new MethodNodeWrapper(mn);
            wrapper.getMethodNode().tryCatchBlocks.removeIf(n -> {
                if (new TryCatchWrapper(n, wrapper).isRemovableRuntime()) {
                    this.removed++;
                    return true;
                }
                return false;
            });
        }));

        logger.atInfo().log("Removed " + removed + " RuntimeExceptions");
    }

}
