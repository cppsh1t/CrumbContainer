package com.crumb.core;

import com.crumb.definition.BeanDefinition;
import com.crumb.definition.ScopeType;
import com.crumb.proxy.DefaultProxyFactory;
import com.crumb.util.ReflectUtil;
import com.crumb.util.StringUtil;
import org.apache.ibatis.session.SqlSessionFactory;

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
    protected void loadMappers(SqlSessionFactory sqlSessionFactory) {
        String packName = ReflectUtil.getTopLevelPackage(configClass.getPackageName());
        sqlSessionFactory.getConfiguration().addMappers(packName);
        hasAddMappers = true;
    }
}
