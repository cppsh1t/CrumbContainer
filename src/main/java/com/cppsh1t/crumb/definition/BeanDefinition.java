package com.cppsh1t.crumb.definition;

import com.cppsh1t.crumb.annotation.ScopeType;

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
