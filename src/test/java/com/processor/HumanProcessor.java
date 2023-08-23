package com.processor;

import com.crumb.annotation.Component;
import com.crumb.beanProcess.BeanPostProcessor;
import com.crumb.beanProcess.DestructionAwareBeanPostProcessor;

@Component
public class HumanProcessor implements DestructionAwareBeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (beanName.equals("human")) {
            System.out.println("before");
        }
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) {
        if (beanName.equals("human")) {
            System.out.println("before des");
        }
    }
}
