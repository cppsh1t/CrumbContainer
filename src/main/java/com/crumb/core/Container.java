package com.crumb.core;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.crumb.beanProcess.BeanPostProcessor;
import com.crumb.definition.BeanDefinition;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.function.Predicate;

public interface Container {

    /**
     * 根据注册的类型来获取Bean
     * @param clazz 注册的类型
     * @param <T> 注册类型的实例类型
     * @return 对应类型的Bean
     */
    <T> T getBean(Class<T> clazz);

    /**
     * 根据注册的名字来获取Bean
     * @param name 注册的名字
     * @return 对应名字的Bean
     */
    Object getBean(String name);

    /**
     * 注册单例Bean
     * @param definition 注册的BeanDefinition
     * @param object 注册的单例
     * @return 是否注册成功
     */
    boolean registerBean(BeanDefinition definition, Object object);

    /**
     * 根据类型获取其BeanDefinition
     * @param clazz 注册的类型
     * @return 对应的BeanDefinition
     */
    BeanDefinition getBeanDefinition(Class<?> clazz);

    /**
     * 根据名字获取其对应的BeanDefinition
     * @param name 注册的名字
     * @return 对应的BeanDefinition
     */
    BeanDefinition getBeanDefinition(String name);

    /**
     * 返回所有符合条件的BeanDefinition
     * @param predicate 用于判断是否符合条件的函数
     * @return 所有符合条件的BeanDefinition
     */
    BeanDefinition[] getBeanDefinition(Predicate<BeanDefinition> predicate);

    /**
     * 返回所有的PostProcessor
     * @return 所有的PostProcessor
     */
    Set<BeanPostProcessor> getBeanPostProcessors();

    /**
     * 打印已注册的Bean其BeanDefinition
     */
    void logBeanDefs();

    /**
     * 设置是否可以覆写注册
     * @param canOverride 是否可以覆写注册
     */
    void setOverride(boolean canOverride);

    /**
     * 关闭容器，对所有Bean做销毁处理
     */
    void close();

    /**
     * 设置Logger的级别
     * @param level Logger的级别
     */
     static void setLoggerLevel(Level level) {
        Logger logger = (Logger) LoggerFactory.getLogger("com.crumb");
        logger.setLevel(level);
    }
}
