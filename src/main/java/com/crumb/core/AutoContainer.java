package com.crumb.core;

import com.crumb.data.SqlSessionFactoryBean;
import com.crumb.definition.BeanDefinition;
import com.crumb.definition.ScopeType;
import com.crumb.proxy.DefaultProxyFactory;
import com.crumb.util.ReflectUtil;
import com.crumb.util.StringUtil;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.sql.DataSource;

public class AutoContainer extends AbstractContainer{

    public AutoContainer(Class<?> configClass) {
        super(configClass);
    }

    @Override
    protected void initContainerChildrenModules() {
        scanner = new AutoBeanScanner();
        objectFactory = new DefaultObjectFactory(this::getBeanInside, this::getBean);
        valuesFactory = new DefaultValuesFactory();
        proxyFactory = new DefaultProxyFactory(this::getBeanInside);
        lifeCycle = new DefaultBeanLifeCycle(valuesFactory::setPropsValue, this::injectBean,
                this::proxyBean, this.postProcessors);
    }

    @Override
    protected void setContainerProps() {
        super.setContainerProps();
        var dateSource = makeInsideDataSource();
        if (dateSource != null) {
            var dataSourceDef = new BeanDefinition(DataSource.class, HikariDataSource.class, "dataSource", ScopeType.SINGLETON);
            registerBean(dataSourceDef, dateSource);
            var sessionFactoryBean = makeInsideSqlSessionFactoryBean(dateSource);
            var sessionDef = new BeanDefinition(SqlSessionFactoryBean.class, SqlSessionFactoryBean.class,
                    "sqlSessionFactoryBean", ScopeType.SINGLETON);
            registerBean(sessionDef, sessionFactoryBean);
        }

    }

    private DataSource makeInsideDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        var jdbcUrl = (String) valuesFactory.getPropValueNoThrow("crumb.data.jdbcUrl");
        if (jdbcUrl == null) {
            return null;
        } else {
            dataSource.setJdbcUrl(jdbcUrl);
        }
        var username = (String) valuesFactory.getPropValueNoThrow("crumb.data.username");
        if (username != null) {
            dataSource.setUsername(username);
        } else {
            return null;
        }
        var password = (String) valuesFactory.getPropValueNoThrow("crumb.data.password");
        if (password != null) {
            dataSource.setPassword(password);
        } else {
            return null;
        }
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return dataSource;
    }

    private SqlSessionFactoryBean makeInsideSqlSessionFactoryBean(DataSource dataSource){
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        return bean;
    }

    @Override
    protected void loadMappers(SqlSessionFactory sqlSessionFactory) {
        String packName = ReflectUtil.getTopLevelPackage(configClass.getPackageName());
        sqlSessionFactory.getConfiguration().addMappers(packName);
        hasAddMappers = true;
    }
}
