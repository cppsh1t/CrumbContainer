package com.crumb.beanProcess;

public interface BeanPostProcessor {

    default public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    default public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }
}
