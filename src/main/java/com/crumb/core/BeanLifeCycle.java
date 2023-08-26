package com.crumb.core;

import com.crumb.definition.BeanDefinition;

public interface BeanLifeCycle {

    Object makeLifeCycle(Object origin, BeanDefinition definition);

    void overLifeCycle(Object bean, String beanName);
}
