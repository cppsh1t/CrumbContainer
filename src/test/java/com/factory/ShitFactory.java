package com.factory;

import com.entity.Shit;
import org.crumb.annotation.Component;
import org.crumb.container.FactoryBean;

@Component
public class ShitFactory implements FactoryBean<Shit> {


    @Override
    public Object getObject() {
        return new Shit();
    }
}
