package com.service;

import com.crumb.annotation.Autowired;
import com.crumb.web.Service;
import com.entity.Stone;

@Service
public class SleepServiceImpl implements SleepService {

    @Autowired
    Stone stone;

    @Override
    public void sleep() {
        System.out.println(stone + " is sleeping");
    }
}
