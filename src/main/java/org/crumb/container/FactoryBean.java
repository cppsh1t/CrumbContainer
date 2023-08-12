package org.crumb.container;

public interface FactoryBean<T> {

    default boolean isSingleton() {
        return true;
    }

    Object getObject();

}
