package com.config;

import com.entity.Foo;
import org.crumb.annotation.*;

import java.util.Random;

@ComponentScan("com")
@Configuration
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
