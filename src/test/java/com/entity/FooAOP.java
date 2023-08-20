package com.entity;

import com.crumb.annotation.*;
import com.crumb.proxy.JoinPoint;

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
