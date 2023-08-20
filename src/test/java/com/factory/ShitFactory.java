package com.factory;

import com.entity.Shit;
import com.crumb.annotation.Component;
import com.crumb.core.FactoryBean;

@Component
public class ShitFactory implements FactoryBean<Shit> {


    @Override
    public Object getObject() {
        return new Shit();
    }
}
