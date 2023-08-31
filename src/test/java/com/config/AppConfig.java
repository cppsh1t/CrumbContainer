package com.config;

import com.crumb.annotation.*;
import java.util.Random;

@MainConfiguration
@Configuration
@EnableAspectProxy
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
