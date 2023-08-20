package com.crumb.core;

public interface BeanFactory {

    <T> T getBean(Class<T> clazz);
}
