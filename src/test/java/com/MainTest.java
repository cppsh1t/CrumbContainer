package com;

import ch.qos.logback.classic.Level;
import com.config.AppConfig;
import com.entity.*;
import org.crumb.core.CrumbContainer;


public class MainTest {

    public static void main(String[] args) {
        CrumbContainer.setLoggerLevel(Level.INFO);
        var container = new CrumbContainer(AppConfig.class);
        System.out.println(container.getBean(Foo.class));
        container.close();
    }
}
