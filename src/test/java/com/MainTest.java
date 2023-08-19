package com;

import ch.qos.logback.classic.Level;
import com.config.AppConfig;
import com.entity.*;
import com.cppsh1t.crumb.core.CrumbContainer;


public class MainTest {

    public static void main(String[] args) {
        CrumbContainer.setLoggerLevel(Level.DEBUG);
        var container = new CrumbContainer(AppConfig.class);
        var catcher = container.getBean(FooCatcher.class);
        catcher.doFooTest();
        container.close();
    }
}
