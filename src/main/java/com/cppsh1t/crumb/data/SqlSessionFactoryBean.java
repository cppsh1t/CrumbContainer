package com.cppsh1t.crumb.data;

import com.cppsh1t.crumb.core.FactoryBean;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.ibatis.session.Configuration;

import javax.sql.DataSource;

public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory> {

    private DataSource dataSource;

    private final TransactionFactory transactionFactory = new JdbcTransactionFactory();

    private Environment environment;

    private Configuration configuration;


    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        environment = new Environment("development", transactionFactory, dataSource);
        configuration = new org.apache.ibatis.session.Configuration(environment);
    }


    @Override
    public Object getObject() {
        return new SqlSessionFactoryBuilder().build(configuration);
    }
}
