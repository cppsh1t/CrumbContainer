package com.crumb.core;

import java.lang.reflect.Method;

public interface ObjectFactory extends BeanFactory{

    Object getBean(Method method, Object invoker);

    void injectBean(Object bean);
}
