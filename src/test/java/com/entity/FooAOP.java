package com.entity;

import com.cppsh1t.crumb.annotation.*;
import com.cppsh1t.crumb.proxy.JoinPoint;

@Component
@Aspect(IFoo.class)
public class FooAOP {

    @Before("test")
    public void before() {
        System.out.println("before test");
    }

    @After("test")
    public void after() {
        System.out.println("after test");
    }

    @Around("toString")
    public Object changeToString(JoinPoint joinPoint) {
        return "bull shit";
    }

}
