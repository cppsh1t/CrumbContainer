package com.entity;

import org.crumb.annotation.*;

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
