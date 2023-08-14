package com.factory;

import com.entity.Shit;
import org.crumb.annotation.Component;
import org.crumb.core.FactoryBean;

@Component
public class ShitFactory implements FactoryBean<Shit> {


    @Override
    public Object getObject() {
        return new Shit();
    }
}
