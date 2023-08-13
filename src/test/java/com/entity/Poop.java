package com.entity;

import org.crumb.annotation.Component;
import org.crumb.annotation.Scope;
import org.crumb.annotation.ScopeType;
import org.crumb.annotation.Values;

@Component
@Scope(ScopeType.PROTOTYPE)
public class Poop {

    @Values("poop.weight")
    private int weight;

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
