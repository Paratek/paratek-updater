package io.paratek.rs.util;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

public class BytecodeUtils {


    /**
     * Converts a ClassNode to a byte array
     * @param cn
     * @return
     */
    public static byte[] getClassNodeBytes(ClassNode cn) {
        ClassWriter cw = new ClassWriter(0);
        cn.accept(cw);
        return cw.toByteArray();
    }

}
