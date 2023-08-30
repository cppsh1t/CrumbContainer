package com;

import ch.qos.logback.classic.Level;
import com.config.AppConfig;
import com.crumb.core.Container;
import com.crumb.core.DefaultContainer;
import com.crumb.core.EnhancedContainer;
import com.crumb.core.MainContainer;
import com.entity.IFoo;
import com.service.SleepService;




public class MainTest {

    public static void main(String[] args) {
//        Container.setLoggerLevel(Level.DEBUG);
        var container = MainContainer.getContainer(DefaultContainer.class);
        var obj =  container.getBean(IFoo.class);
        container.close();
    }
}
