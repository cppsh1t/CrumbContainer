package com.cppsh1t.crumb.core;

public interface FactoryBean<T> {

    default boolean isSingleton() {
        return true;
    }

    Object getObject();

}
