package com;

import com.config.AppConfig;
import com.entity.*;
import org.crumb.container.CrumbContainer;


public class MainTest {

    public static void main(String[] args) {
        var container = new CrumbContainer(AppConfig.class);
        container.getBean(Shit.class);
        container.getBean(Shit.class);
    }
}
