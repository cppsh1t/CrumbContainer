package com.crumb.exception;

import com.crumb.definition.BeanDefinition;

public class BeanNotFoundException extends RuntimeException{

    public BeanNotFoundException(BeanDefinition definition) {
        super("Bean: " + definition + " Not Found");
    }

    public BeanNotFoundException(Class clazz) {
        super("Bean which class is " + clazz.getName() + " Not Found");
    }
}
