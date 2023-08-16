package com;

import com.config.AppConfig;
import com.entity.*;
import org.crumb.core.CrumbContainer;


public class MainTest {

    public static void main(String[] args) {
        var container = new CrumbContainer(AppConfig.class);
        System.out.println(container.getBean(Foo.class));

    }
}
