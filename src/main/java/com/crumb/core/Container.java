package com.crumb.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.crumb.beanProcess.BeanPostProcessor;
import com.crumb.definition.BeanDefinition;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Predicate;

public interface Container {

    <T> T getBean(Class<T> clazz);

    Object getBean(String name);

    boolean registerBean(BeanDefinition definition, Object object);

    BeanDefinition getBeanDefinition(Class<?> clazz);

    BeanDefinition getBeanDefinition(String name);

    BeanDefinition[] getBeanDefinition(Predicate<BeanDefinition> predicate);

    Set<BeanPostProcessor> getBeanPostProcessors();

    void logBeanDefs();

    void setOverride(boolean canOverride);

    void close();

     static void setLoggerLevel(Level level) {
        Logger logger = (Logger) LoggerFactory.getLogger("com.crumb");
        logger.setLevel(level);
    }
}
