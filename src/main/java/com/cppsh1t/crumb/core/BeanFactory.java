package com.cppsh1t.crumb.core;

public interface BeanFactory {

    <T> T getBean(Class<T> clazz);
}
