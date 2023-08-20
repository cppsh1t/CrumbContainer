package com.entity;

import com.crumb.annotation.Autowired;
import com.crumb.annotation.Component;
import com.crumb.annotation.Scope;
import com.crumb.definition.ScopeType;

@Component
@Scope(ScopeType.PROTOTYPE)
public class Stone {

    @Autowired
    private int weight;

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
