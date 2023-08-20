package com.entity;

import com.cppsh1t.crumb.annotation.Component;
import com.cppsh1t.crumb.annotation.Lazy;
import com.cppsh1t.crumb.annotation.PostConstruct;
import com.cppsh1t.crumb.annotation.PreDestroy;
import com.cppsh1t.crumb.beanProcess.DisposableBean;
import com.cppsh1t.crumb.beanProcess.InitializingBean;

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
