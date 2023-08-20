package com.entity;

import com.crumb.annotation.Autowired;
import com.crumb.annotation.Component;

@Component
public class Human {
    String name;

    @Autowired
    public Human(String name) {
        this.name = name;
        System.out.println(this + " born");
    }

    public void introduce() {
        System.out.println("I am " + name);
    }

    public void setName(String name) {
        this.name = name;
        System.out.println("名字改了");
    }

}
