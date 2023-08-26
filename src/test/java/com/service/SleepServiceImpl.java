package com.service;

import com.crumb.annotation.Autowired;
import com.crumb.annotation.Resource;
import com.crumb.web.Service;
import com.entity.Stone;

@Service
public class SleepServiceImpl implements SleepService {

    @Resource
    String retardName;

    @Override
    public void sleep() {
        System.out.println(retardName + " is sleeping");
    }
}
