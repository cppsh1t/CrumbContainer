package com.cppsh1t.crumb.builder;

import com.cppsh1t.crumb.annotation.Bean;
import com.cppsh1t.crumb.annotation.Component;
import com.cppsh1t.crumb.annotation.Scope;
import com.cppsh1t.crumb.definition.BeanDefinition;
import com.cppsh1t.crumb.definition.Empty;
import com.cppsh1t.crumb.definition.ScopeType;
import com.cppsh1t.crumb.util.ClassConverter;

import java.lang.reflect.Method;

public class BeanDefinitionBuilder {

    public static Class<?> getKeyType(Class<?> clazz) {
        var anno = clazz.getAnnotation(Component.class);
        Class<?> keyType = anno.value();
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

    public static BeanDefinition getComponentDef(Class<?> clazz) {
        var keyType = getKeyType(clazz);
        var scope = getScope(clazz);
        return new BeanDefinition(keyType, clazz, scope);
    }

    public static BeanDefinition getMethodBeanDef(Method method) {
        var keyType = getKeyType(method);
        var clazz = method.getReturnType();
        var scope = ScopeType.SINGLETON;
        return new BeanDefinition(keyType, clazz, scope);
    }
}
