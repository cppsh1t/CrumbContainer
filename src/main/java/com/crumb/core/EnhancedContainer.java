package com.crumb.core;


import com.crumb.proxy.DefaultProxyFactory;

public class EnhancedContainer extends AbstractContainer{


    public EnhancedContainer(Class<?> configClass) {
        super(configClass);
    }

    @Override
    protected void initContainerChildrenModules() {
        scanner = new ClassGraphBeanScanner();
        objectFactory = new DefaultObjectFactory(this::getBeanInside, this::getBean);
        valuesFactory = new DefaultValuesFactory();
        proxyFactory = new DefaultProxyFactory(this::getBeanInside);
        lifeCycle = new DefaultBeanLifeCycle(valuesFactory::setPropsValue, this::injectBean,
                this::proxyBean, this.postProcessors);
    }
}
