package com.cppsh1t.crumb.exception;

import com.cppsh1t.crumb.definition.BeanDefinition;

public class BeanNotFoundException extends RuntimeException{

    public BeanNotFoundException(BeanDefinition definition) {
        super("Bean: " + definition + " Not Found");
    }

    public BeanNotFoundException(Class clazz) {
        super("Bean which class is " + clazz.getName() + " Not Found");
    }
}
