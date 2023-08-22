package com.crumb.definition;

public class BeanDefinition {

    public final Class<?> keyType;
    public final Class<?> clazz;
    public final ScopeType scope;
    public final String name;

    public BeanDefinition(Class<?> keyType, Class<?> clazz, String name, ScopeType scope) {
        this.keyType = keyType;
        this.clazz = clazz;
        this.scope = scope;
        this.name = name;
    }

    @Override
    public String toString() {
        return "{ keyType: " + keyType.getName() + ",class: " + clazz.getName() + ", scope: " + scope + " }";
    }
}
