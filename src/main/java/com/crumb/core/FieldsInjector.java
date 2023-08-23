package com.crumb.core;

import com.crumb.definition.BeanDefinition;

public interface FieldsInjector {

    void inject(Object bean, BeanDefinition definition);
}
