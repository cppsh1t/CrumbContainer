package com.config;

import com.crumb.annotation.*;
import com.crumb.data.EnableTransactionManagement;


import java.util.Random;

@MainConfiguration
@Configuration
@EnableAspectProxy
@EnableTransactionManagement
public class AppConfig {

    @Bean(name = "retardName")
    @Lazy
    public String getName() {
        return "Xqc";
    }

    @Bean
    public int getInt() {
        return new Random().nextInt(30);
    }

}
