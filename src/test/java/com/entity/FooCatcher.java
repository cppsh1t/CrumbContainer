package com.entity;

import org.crumb.annotation.Autowired;
import org.crumb.annotation.Component;
import org.crumb.annotation.Lazy;

@Component
@Lazy
public class FooCatcher {

    @Autowired
    Foo foo;

    public void doFooTest() {
        foo.test();
    }

}
