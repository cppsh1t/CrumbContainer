package com.config;

import org.crumb.annotation.Bean;
import org.crumb.annotation.ComponentScan;
import org.crumb.annotation.Configuration;

import java.util.Random;

@ComponentScan("com")
@Configuration
public class AppConfig {

    @Bean
    public String getName() {
        return "Taihou";
    }

    @Bean
    public int getInt() {
        return new Random().nextInt(30);
    }

}
