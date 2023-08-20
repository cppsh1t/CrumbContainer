package com.cppsh1t.crumb.definition;

public class BeanDefinition {

    public final Class<?> clazz;
    public final ScopeType scope;

    public BeanDefinition(Class<?> clazz, ScopeType scope) {
        this.clazz = clazz;
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "{ class: " + clazz.getName() + ", scope: " + scope + " }";
    }
}
