package com.entity;

import org.crumb.annotation.Autowired;
import org.crumb.annotation.Component;
import org.crumb.annotation.Lazy;

@Component
@Lazy
public class Bitch {
    String name;

    @Autowired
    Dildo dildo;

    @Autowired
    public Bitch(String name) {
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

    public void rut() {
        introduce();
        System.out.println("And I am using a dildo which length were " + dildo.getLength() + "cm to FUCK my asshole!");
    }
}
