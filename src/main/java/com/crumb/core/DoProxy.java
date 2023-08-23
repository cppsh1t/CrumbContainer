package com.crumb.core;

import com.crumb.definition.BeanDefinition;

public interface DoProxy {

    Object proxyBean(Object bean, BeanDefinition definition);
}
