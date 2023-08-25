package com.crumb.core;

import com.crumb.beanProcess.BeanPostProcessor;
import com.crumb.definition.BeanDefinition;

import java.util.Set;
import java.util.function.Predicate;

public interface Container {

    public <T> T getBean(Class<T> clazz);

    public Object getBean(String name);

    public boolean registerBean(BeanDefinition definition, Object object);

    public BeanDefinition getBeanDefinition(Class<?> clazz);

    public BeanDefinition getBeanDefinition(String name);

    public BeanDefinition[] getBeanDefinition(Predicate<BeanDefinition> predicate);

    public Set<BeanPostProcessor> getBeanPostProcessors();

    public void logBeanDefs();

    public void setOverride(boolean canOverride);

    public void close();
}
