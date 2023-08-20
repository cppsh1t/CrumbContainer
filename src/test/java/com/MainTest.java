package com;

import ch.qos.logback.classic.Level;
import com.config.AppConfig;
import com.entity.*;
import com.cppsh1t.crumb.core.CrumbContainer;
import com.mapper.TestMapper;


public class MainTest {

    public static void main(String[] args) {
//        CrumbContainer.setLoggerLevel(Level.DEBUG);
        var container = new CrumbContainer(AppConfig.class);
//        var foo = container.getBean(IFoo.class);
//        foo.test();
        var mapper = container.getBean(TestMapper.class);
        mapper.selectStudents().forEach(System.out::println);
    }
}
