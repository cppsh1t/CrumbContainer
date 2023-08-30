package com;

import ch.qos.logback.classic.Level;
import com.config.AppConfig;
import com.crumb.core.Container;
import com.crumb.core.DefaultContainer;
import com.crumb.core.EnhancedContainer;
import com.crumb.core.MainContainer;
import com.crumb.util.ReflectUtil;
import com.entity.IFoo;
import com.service.SleepService;




public class MainTest {

    public static void main(String[] args) {
        Container.setLoggerLevel(Level.INFO);
        var container = MainContainer.getContainer();
        var foo =  container.getBean(IFoo.class);
        foo.test();
        System.out.println(foo);
        container.close();
    }
}
