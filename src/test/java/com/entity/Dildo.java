package com.entity;

import org.crumb.annotation.Autowired;
import org.crumb.annotation.Component;
import org.crumb.annotation.Lazy;

@Component
@Lazy
public class Dildo {

    int length;

    @Autowired
    Bitch bitch;

    @Autowired
    public Dildo(Integer length) {
        this.length = length;
    }

    public void checkUser() {
        System.out.println(bitch.name + " is using me");
    }

    public int getLength() {
        return length;
    }
}
