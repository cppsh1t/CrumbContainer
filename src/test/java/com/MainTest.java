package com;

import com.config.AppConfig;
import com.entity.*;
import org.crumb.container.CrumbContainer;
import org.crumb.container.LoggerManager;


public class MainTest {

    public static void main(String[] args) {
//        LoggerManager.closeLogger();
        var container = new CrumbContainer(AppConfig.class);
        container.getBean(Shit.class);
        container.getBean(Shit.class);
    }
}
