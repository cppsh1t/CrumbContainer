package com.crumb.core;

import com.crumb.annotation.PostConstruct;
import com.crumb.annotation.PreDestroy;
import com.crumb.beanProcess.BeanPostProcessor;
import com.crumb.beanProcess.DestructionAwareBeanPostProcessor;
import com.crumb.beanProcess.DisposableBean;
import com.crumb.beanProcess.InitializingBean;
import com.crumb.definition.BeanDefinition;
import com.crumb.exception.MethodRuleException;
import com.crumb.proxy.ProxyObject;
import com.crumb.util.ProxyUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class DefaultBeanLifeCycle implements BeanLifeCycle {
    protected final Consumer<Object> valuesInjector;
    protected final FieldsInjector fieldsInjector;
    protected final ProxyProvider proxyProvider;
    protected final Set<BeanPostProcessor> processors;
    protected Set<DestructionAwareBeanPostProcessor> desProcessors;
    protected boolean hasInitDesProcessor = false;

    public DefaultBeanLifeCycle(Consumer<Object> valuesInjector, FieldsInjector fieldsInjector,
                                ProxyProvider proxyProvider, Set<BeanPostProcessor> processors) {
        this.valuesInjector = valuesInjector;
        this.fieldsInjector = fieldsInjector;
        this.proxyProvider = proxyProvider;
        this.processors = processors;
    }


    public Object makeLifeCycle(Object origin, BeanDefinition definition) {
        String beanName = definition.name;
        valuesInjector.accept(origin);
        var proxyInstance = proxyProvider.proxyBean(origin, definition);
        fieldsInjector.inject(origin, definition);

        for (var processor : processors) {
            proxyInstance = processor.postProcessBeforeInitialization(proxyInstance, beanName);
        }

        initBean(proxyInstance);

        for (var processor : processors) {
            proxyInstance = processor.postProcessAfterInitialization(proxyInstance, beanName);
        }

        return proxyInstance;
    }

    public void endLifeCycle(Object bean, String beanName) {
        if (!hasInitDesProcessor) {
            hasInitDesProcessor = true;
            this.desProcessors = processors.stream()
                    .filter(p -> p instanceof DestructionAwareBeanPostProcessor)
                    .map(p -> (DestructionAwareBeanPostProcessor) p).collect(Collectors.toSet());
        }

        boolean needDes = true;
        for (var des : desProcessors) {
            if (!des.requiresDestruction(bean)) {
                needDes = false;
                break;
            }
        }

        if (needDes) {
            for (var des : desProcessors) {
                des.postProcessBeforeDestruction(bean, beanName);
            }
        }
        destroyBean(bean);

    }

    protected void initBean(Object bean) {
        Class<?> clazz;

        if (bean instanceof ProxyObject) {
            clazz = ProxyUtil.getOriginClass(bean.getClass());
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

    protected void destroyBean(Object bean) {
        Class<?> clazz;

        if (bean instanceof ProxyObject) {
            clazz = ProxyUtil.getOriginClass(bean.getClass());
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
