package com.crumb.util;

import java.util.HashMap;
import java.util.Map;

public class ClassConverter {

    private static final Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<>();

    static {
        primitiveToWrapper.put(boolean.class, Boolean.class);
        primitiveToWrapper.put(byte.class, Byte.class);
        primitiveToWrapper.put(short.class, Short.class);
        primitiveToWrapper.put(int.class, Integer.class);
        primitiveToWrapper.put(long.class, Long.class);
        primitiveToWrapper.put(float.class, Float.class);
        primitiveToWrapper.put(double.class, Double.class);
        primitiveToWrapper.put(char.class, Character.class);
    }

    public static Class<?> convertPrimitiveType(Class<?> clazz) {
        Class<?> wrapperClass = primitiveToWrapper.get(clazz);
        return wrapperClass != null ? wrapperClass : clazz;
    }
}
