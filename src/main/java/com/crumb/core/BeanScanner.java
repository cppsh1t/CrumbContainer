package com.crumb.core;

import com.crumb.definition.BeanDefinition;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BeanScanner {

    List<BeanDefinition> getBeanDefinition(Class<?> clazz);

    Map<Class<?>, BeanDefinition> getFactoryBeanDefinition(Set<BeanDefinition> definitions);

    List<Method> getBeanMethod(Class<?> clazz);

    Map<Class<?>, Method> getFactoryBeanMethods(Set<Method> methods);

    Map<Class<?>, BeanDefinition> getAopBeanDefinition(Set<BeanDefinition> definitions);
}
