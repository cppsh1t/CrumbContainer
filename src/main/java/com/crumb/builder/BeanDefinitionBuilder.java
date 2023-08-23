package com.crumb.builder;

import com.crumb.annotation.Bean;
import com.crumb.annotation.Component;
import com.crumb.annotation.Scope;
import com.crumb.definition.BeanDefinition;
import com.crumb.definition.Empty;
import com.crumb.definition.ScopeType;
import com.crumb.exception.BeanDefinitionParseException;
import com.crumb.util.ClassConverter;
import com.crumb.util.StringUtil;
import com.crumb.web.Controller;

import java.lang.reflect.Method;

public class BeanDefinitionBuilder {

    public static Class<?> getKeyType(Class<?> clazz) {
        Class<?> keyType;
        if (clazz.isAnnotationPresent(Component.class)) {
            keyType = clazz.getAnnotation(Component.class).value();
        } else if (clazz.isAnnotationPresent(Controller.class)) {
            keyType = clazz.getAnnotation(Controller.class).type();
        } else {
            throw new BeanDefinitionParseException();
        }

        if (keyType == Empty.class) {
            keyType = clazz;
        }
        return keyType;
    }

    public static Class<?> getKeyType(Method method) {
        var anno = method.getDeclaredAnnotation(Bean.class);
        Class<?> keyType = anno.value();
        if (keyType == Empty.class) {
            keyType = method.getReturnType();
        }
        keyType = ClassConverter.convertPrimitiveType(keyType);
        return keyType;
    }

    public static ScopeType getScope(Class<?> clazz) {
        var anno = clazz.getAnnotation(Scope.class);
        if (anno != null) {
            return anno.value();
        } else {
            return ScopeType.SINGLETON;
        }
    }

    public static String getName(Class<?> clazz) {
        String name;
        if (clazz.isAnnotationPresent(Component.class)) {
            name = clazz.getAnnotation(Component.class).name();
        } else if (clazz.isAnnotationPresent(Controller.class)) {
            name = clazz.getAnnotation(Controller.class).value();
        } else {
            throw new BeanDefinitionParseException();
        }

        if (name.isEmpty()) {
            name = StringUtil.lowerFirst(clazz.getSimpleName());
        }
        return name;
    }

    public static String getName(Method method) {
        String name = method.getDeclaredAnnotation(Bean.class).name();

        if (name.isEmpty()) {
            name = StringUtil.lowerFirst(method.getName());
        }
        return name;
    }

    public static BeanDefinition getComponentDef(Class<?> clazz) {
        var keyType = getKeyType(clazz);
        var scope = getScope(clazz);
        var name = getName(clazz);
        return new BeanDefinition(keyType, clazz, name, scope);
    }

    public static BeanDefinition getMethodBeanDef(Method method) {
        var keyType = getKeyType(method);
        var clazz = method.getReturnType();
        var scope = ScopeType.SINGLETON;
        var name = getName(method);
        return new BeanDefinition(keyType, clazz, name, scope);
    }
}
