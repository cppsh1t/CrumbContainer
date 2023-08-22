package com.entity;

import com.crumb.annotation.Autowired;
import com.crumb.annotation.Component;
import com.crumb.annotation.Lazy;

@Component(name = "catcher")
@Lazy
public class FooCatcher {

    @Autowired
    IFoo foo;

    public void doFooTest() {
        foo.test();
    }

}
