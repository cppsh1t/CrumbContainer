package com;

import ch.qos.logback.classic.Level;
import com.config.AppConfig;
import com.crumb.core.CrumbContainer;
import com.entity.FooCatcher;
import com.entity.Human;
import com.entity.Stone;


public class MainTest {

    public static void main(String[] args) {
        CrumbContainer.setLoggerLevel(Level.DEBUG);
        var container = new CrumbContainer(AppConfig.class);
        System.out.println(container.getBean(Human.class));
        container.close();
    }
}
