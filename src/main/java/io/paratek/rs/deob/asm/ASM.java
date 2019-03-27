package io.paratek.rs.deob.asm;

import jdk.internal.org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ASM {

    private static final List<Character> TYPES = Arrays.asList('Z', 'B', 'J', 'I', 'S', 'D', 'F', 'V');

    public static List<String> extractDesc(final MethodNode methodNode) {
        final String desc = methodNode.desc;
        final List<String> strings = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean openObject = false;
        for (char c : desc.toCharArray()) {
            if (c == '(' || c == ')') {
                continue;
            }
            current.append(c);
            if (c == '[') {
                openObject = false;
            } else if (c == 'L') {
                openObject = true;
            } else if (c == ';') {
                openObject = false;
                strings.add(current.toString());
                current = new StringBuilder();
            } else {
                if (TYPES.contains(c) && !openObject) {
                    strings.add(current.toString());
                    current = new StringBuilder();
                    openObject = false;
                }
            }
        }
        return strings;
    }

}
