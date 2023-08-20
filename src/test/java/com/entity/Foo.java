package com.entity;

import com.crumb.annotation.Component;
import com.crumb.annotation.Lazy;
import com.crumb.annotation.PostConstruct;
import com.crumb.annotation.PreDestroy;
import com.crumb.beanProcess.DisposableBean;
import com.crumb.beanProcess.InitializingBean;

@Component(IFoo.class)
@Lazy
public class Foo implements InitializingBean, DisposableBean, IFoo {

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
