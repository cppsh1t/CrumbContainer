package com.config;

import com.entity.Foo;
import org.crumb.annotation.Bean;
import org.crumb.annotation.ComponentScan;
import org.crumb.annotation.Configuration;
import org.crumb.annotation.Lazy;

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

    @Bean
    @Lazy
    public Foo foo() {
        return new Foo();
    }

}
