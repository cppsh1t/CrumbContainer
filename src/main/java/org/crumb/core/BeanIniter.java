package org.crumb.core;

import lombok.extern.slf4j.Slf4j;
import org.crumb.annotation.PostConstruct;
import org.crumb.beanProcess.InitializingBean;
import org.crumb.exception.MethodRuleException;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;


public class BeanIniter {

    public void initBean(Object bean) {
        Class<?> clazz = bean.getClass();

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
