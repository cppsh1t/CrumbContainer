package com;

import com.config.AppConfig;
import com.entity.*;
import org.crumb.container.CrumbContainer;
import org.crumb.container.LoggerManager;


public class MainTest {

    public static void main(String[] args) {
        LoggerManager.openLogger();
        var container = new CrumbContainer(AppConfig.class);
        var poop = container.getBean(Poop.class);
        System.out.println(poop.getWeight());
    }
}
