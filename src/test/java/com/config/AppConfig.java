package com.config;

import com.cppsh1t.crumb.annotation.*;

import java.util.Random;

@ComponentScan("com")
@Configuration
@EnableAspectProxy
public class AppConfig {

    @Bean
    @Lazy
    public String getName() {
        return "Xqc";
    }

    @Bean
    public int getInt() {
        return new Random().nextInt(30);
    }


}
