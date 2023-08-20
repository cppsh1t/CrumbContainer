package com.crumb.definition;

public class BeanDefinition {

    public final Class<?> keyType;
    public final Class<?> clazz;
    public final ScopeType scope;

    public BeanDefinition(Class<?> keyType, Class<?> clazz, ScopeType scope) {
        this.keyType = keyType;
        this.clazz = clazz;
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "{ keyType: " + keyType.getName() + ",class: " + clazz.getName() + ", scope: " + scope + " }";
    }
}
