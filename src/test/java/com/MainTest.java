package com;

import com.config.AppConfig;
import com.entity.*;
import org.crumb.core.CrumbContainer;


public class MainTest {

    public static void main(String[] args) {
        var container = new CrumbContainer(AppConfig.class);
        var stone = container.getBean(Stone.class);
        System.out.println(stone.getWeight());
    }
}
