package com.crumb.util;

import com.crumb.exception.DefaultConstructorException;
import com.crumb.exception.CreateInstanceException;
import com.crumb.exception.MethodInvocationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class ReflectUtil {

    /**
     * 调用类的无参构造函数创建实例
     *
     * @param clazz 要创建的实例的类
     * @return 创建的实例
     */
    public static <T> T createInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new DefaultConstructorException(clazz);
    }

    public static <T> T createInstance(Constructor<T> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new CreateInstanceException(constructor, args);
    }

    public static Object invokeMethod(Method method, Object invoker, Object... args) {
        try {
            return method.invoke(invoker, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new MethodInvocationException(method);
        }
    }

    public static Constructor<?> getConstructorWithAnnotation(Class<?> clazz, Class<? extends Annotation> annoClass) {
        return Arrays.stream(clazz.getDeclaredConstructors())
                .filter(con -> con.isAnnotationPresent(annoClass))
                .findFirst()
                .orElse(null);
    }

    public static boolean hasAnnotationOnField(Class<?> clazz, Class<? extends Annotation> anno) {
        var result = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(anno))
                .findFirst().orElse(null);
        return result != null;
    }

    public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> anno) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(anno))
                .collect(Collectors.toList());
    }

    public static void setFieldValue(Field field, Object target, Object value) {
        String setterName = "set" + StringUtil.toUpperFirstChar(field.getName());
        try {
            Method setMethod = target.getClass().getMethod(setterName, field.getType());
            ReflectUtil.invokeMethod(setMethod, target, value);
        } catch (NoSuchMethodException e) {
            field.setAccessible(true);
            try {
                field.set(target, value);
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static Class<?> getFirstParamFromGenericInterface(Class<?> clazz, Class<?> interfacz) {
        return Arrays.stream(clazz.getGenericInterfaces())
                .filter(type -> type instanceof ParameterizedType)
                .map(type -> (ParameterizedType) type)
                .filter(type -> type.getRawType() == interfacz)
                .map(type -> (Class<?>) type.getActualTypeArguments()[0])
                .findFirst().orElse(null);
    }

}
