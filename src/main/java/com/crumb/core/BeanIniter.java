package com.crumb.core;

import com.crumb.exception.MethodRuleException;
import com.crumb.annotation.PostConstruct;
import com.crumb.beanProcess.InitializingBean;
import com.crumb.proxy.ProxyObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;


public class BeanIniter {

    public void initBean(Object bean) {
        Class<?> clazz;

        if (bean instanceof ProxyObject) {
            clazz = bean.getClass().getSuperclass();
        } else {
            clazz = bean.getClass();
        }

        var postConstructMethod = Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                .findFirst().orElse(null);

        if (postConstructMethod != null) {
            try {
                postConstructMethod.invoke(bean);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new MethodRuleException("postConstructMethod must has noArgs");
            }
        }

        if (bean instanceof InitializingBean initer) {
            initer.afterPropertiesSet();
        }
    }
}
