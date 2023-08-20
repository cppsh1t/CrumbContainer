package com;

import ch.qos.logback.classic.Level;
import com.config.AppConfig;
import com.crumb.core.CrumbContainer;
import com.mapper.TestMapper;


public class MainTest {

    public static void main(String[] args) {
        CrumbContainer.setLoggerLevel(Level.DEBUG);
        var container = new CrumbContainer(AppConfig.class);
        var mapper = container.getBean(TestMapper.class);
        mapper.selectStudents().forEach(System.out::println);
    }
}
