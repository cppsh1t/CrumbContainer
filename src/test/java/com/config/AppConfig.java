package com.config;

import com.crumb.annotation.*;
import com.crumb.data.MapperScan;
import com.crumb.data.SqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Random;

@MainConfiguration
@ComponentScan("com")
@Configuration
@EnableAspectProxy
@MapperScan("com.mapper")
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
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/test");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUsername("username");
        dataSource.setPassword("password");
        return dataSource;
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactoryBean(DataSource dataSource){
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean;
    }
}
