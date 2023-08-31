package com;

import ch.qos.logback.classic.Level;
import com.crumb.annotation.PostConstruct;
import com.crumb.core.Container;
import com.crumb.core.EnhancedContainer;
import com.crumb.core.MainContainer;
import com.crumb.proxy.ProxyObject;
import com.entity.Foo;
import com.entity.IFoo;
import com.entity.Stone;
import com.mapper.TestMapper;

import java.util.Arrays;


public class MainTest {

    private static final Container container;


    static {
        Container.setLoggerLevel(Level.DEBUG);
        container = MainContainer.getContainer();
    }

    public static void main(String[] args) {
        aopTest();
    }

    public static void dataTest() {
        var mapper = container.getBean(TestMapper.class);
        mapper.selectStudents().forEach(System.out::println);
    }

    public static void aopTest() {
        var foo = container.getBean(IFoo.class);
        foo.test();
        var inside = ((ProxyObject) foo).getOrigin();
        System.out.println(inside);
    }

    public static void normalTest() {
        var stone = container.getBean(Stone.class);
        System.out.println(stone.getWeight());
    }

}
