package com.entity;

import com.cppsh1t.crumb.annotation.Autowired;
import com.cppsh1t.crumb.annotation.Component;
import com.cppsh1t.crumb.annotation.Lazy;

@Component
@Lazy
public class FooCatcher {

    @Autowired
    Foo foo;

    public void doFooTest() {
        foo.test();
    }

}
