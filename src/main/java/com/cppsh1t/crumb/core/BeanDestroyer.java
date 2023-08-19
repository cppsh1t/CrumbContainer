package com.cppsh1t.crumb.core;

import com.cppsh1t.crumb.beanProcess.DisposableBean;
import com.cppsh1t.crumb.exception.MethodRuleException;
import com.cppsh1t.crumb.annotation.PreDestroy;

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
