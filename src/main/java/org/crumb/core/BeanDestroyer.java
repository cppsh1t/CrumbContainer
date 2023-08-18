package org.crumb.core;

import org.crumb.annotation.PostConstruct;
import org.crumb.annotation.PreDestroy;
import org.crumb.beanProcess.DisposableBean;
import org.crumb.beanProcess.InitializingBean;
import org.crumb.exception.MethodRuleException;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class BeanDestroyer {

    public void destroyBean(Object bean) {
        Class<?> clazz = bean.getClass();

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
