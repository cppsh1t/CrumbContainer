package com.crumb.core;

import com.crumb.beanProcess.DisposableBean;
import com.crumb.exception.MethodRuleException;
import com.crumb.annotation.PreDestroy;
import com.crumb.proxy.ProxyObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class BeanDestroyer {

    public void destroyBean(Object bean) {
        Class<?> clazz;

        if (bean instanceof ProxyObject) {
            clazz = bean.getClass().getSuperclass();
        } else {
            clazz = bean.getClass();
        }

        var preDestroyMethod = Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(PreDestroy.class))
                .findFirst().orElse(null);

        if (preDestroyMethod != null) {
            try {
                preDestroyMethod.invoke(bean);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                throw new MethodRuleException("postConstructMethod must has noArgs");
            }
        }

        if (bean instanceof DisposableBean dis) {
            dis.destroy();
        }
    }
}
