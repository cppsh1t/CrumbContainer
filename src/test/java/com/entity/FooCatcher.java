package com.entity;

import com.crumb.annotation.Autowired;
import com.crumb.annotation.Component;
import com.crumb.annotation.Lazy;

@Component
@Lazy
public class FooCatcher {

    @Autowired
    Foo foo;

    public void doFooTest() {
        foo.test();
    }

}
