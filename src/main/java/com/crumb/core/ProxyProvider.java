package com.crumb.core;

import com.crumb.definition.BeanDefinition;

public interface ProxyProvider {

    Object proxyBean(Object bean, BeanDefinition definition);
}
