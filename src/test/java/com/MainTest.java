package com;

import ch.qos.logback.classic.Level;
import com.config.AppConfig;
import com.crumb.core.CrumbContainer;
import com.crumb.core.MainContainer;
import com.entity.Human;
import com.service.SleepService;
import com.service.SleepServiceImpl;


public class MainTest {

    public static void main(String[] args) {
        CrumbContainer.setLoggerLevel(Level.DEBUG);
        var container = MainContainer.getContainer();
        container.getBean(SleepService.class).sleep();
    }
}
