package com.entity;

import org.crumb.annotation.Component;
import org.crumb.annotation.Lazy;
import org.crumb.annotation.PostConstruct;
import org.crumb.annotation.PreDestroy;
import org.crumb.beanProcess.DisposableBean;
import org.crumb.beanProcess.InitializingBean;

@Component
@Lazy
public class Foo implements InitializingBean, DisposableBean {

    public Foo() {
        System.out.println("foo!");
    }

    @PostConstruct
    public void init() {
        System.out.println("PostConstruct");
    }

    @PreDestroy
    public void preDestroy() {
        System.out.println("PreDestroy");
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("InitializingBean");
    }

    @Override
    public void destroy() {
        System.out.println("DisposableBean");
    }

    public void test() {
        System.out.println("I am foo");
    }
}
