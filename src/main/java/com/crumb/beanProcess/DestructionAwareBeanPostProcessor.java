package com.crumb.beanProcess;

public interface DestructionAwareBeanPostProcessor extends BeanPostProcessor{

    void postProcessBeforeDestruction(Object bean, String beanName);

    default boolean requiresDestruction(Object bean) {
        return true;
    }
}
