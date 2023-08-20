package com.entity;

import com.cppsh1t.crumb.annotation.Autowired;
import com.cppsh1t.crumb.annotation.Component;
import com.cppsh1t.crumb.annotation.Scope;
import com.cppsh1t.crumb.definition.ScopeType;

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
