package io.paratek.rs.deob.asm;

import jdk.internal.org.objectweb.asm.commons.Remapper;

import java.util.Collections;
import java.util.Map;

public class BetterRemapper extends Remapper {

    private final Map<String, String> mapping;

    public BetterRemapper(Map<String, String> mapping) {
        this.mapping = mapping;
    }

    public BetterRemapper(String oldName, String newName) {
        this.mapping = Collections.singletonMap(oldName, newName);
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        String s = map(owner + '.' + name + desc);
        return s == null ? name : s;
    }

    @Override
    public String mapInvokeDynamicMethodName(String name, String desc) {
        String s = map('.' + name + desc);
        return s == null ? name : s;
    }

    @Override
    public String mapFieldName(String owner, String name, String desc) {
        String s = map(owner + '.' + name + "-" + desc);
        return s == null ? name : s;
    }

    @Override
    public String map(String key) {
        return mapping.get(key);
    }
}
