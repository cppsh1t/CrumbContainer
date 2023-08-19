package com.factory;

import com.entity.Shit;
import com.cppsh1t.crumb.annotation.Component;
import com.cppsh1t.crumb.core.FactoryBean;

@Component
public class ShitFactory implements FactoryBean<Shit> {


    @Override
    public Object getObject() {
        return new Shit();
    }
}
