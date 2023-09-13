package com.crumb.core;


import com.crumb.data.Transactional;
import com.crumb.definition.BeanDefinition;
import com.crumb.proxy.DefaultProxyFactory;
import com.crumb.proxy.TransactionProxyFactory;

import java.util.Arrays;

public class EnhancedContainer extends AbstractContainer{

    public EnhancedContainer(Class<?> configClass) {
        super(configClass);
    }

    @Override
    protected void initContainerChildrenModules() {
        scanner = new ClassGraphBeanScanner();
        objectFactory = new DefaultObjectFactory(this::getBeanInside, this::getBean);
        valuesFactory = new DefaultValuesFactory();
        proxyFactory = new TransactionProxyFactory(this::getBeanInside);
        lifeCycle = new DefaultBeanLifeCycle(valuesFactory::setPropsValue, this::injectBean,
                this::proxyBean, this.postProcessors);
    }

    @Override
    protected Object proxyBean(Object bean, BeanDefinition definition) {
        if (!enableProxy) return bean;

        var keyType = definition.keyType;
        var def = aopDefMap.get(keyType);
        if (!enableTransactionManage) {
            if (def == null) return bean;

            var aopObj = createBean(def);
            log.debug("Will proxy bean: {} with {}", bean, aopObj);
            return proxyFactory.makeProxy(bean, aopObj);
        } else {
            boolean hasTran = Arrays.stream(bean.getClass().getDeclaredMethods())
                    .anyMatch(m -> m.isAnnotationPresent(Transactional.class));
            if (def == null && !hasTran) return bean;

            if (def == null) {
                log.debug("Will proxy Transactional bean: {}", bean);
                return proxyFactory.makeProxy(bean, null);
            } else {
                var aopObj = createBean(def);
                log.debug("Will proxy Transactional bean: {} with {}", bean, aopObj);
                return proxyFactory.makeProxy(bean, aopObj);
            }
        }
    }
}
