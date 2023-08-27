package com.crumb.core;

import com.crumb.annotation.MainConfiguration;
import com.crumb.exception.ContainerConstructorException;
import com.crumb.exception.MainConfigurationNotFoundException;
import com.crumb.util.ReflectUtil;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Constructor;
import java.util.Arrays;


public class MainContainer {
    private static Container instance;

    /**
     * 获取单例容器，自动寻找标记了@MainConfiguration注解的配置类
     * @return 单例容器
     */
    public static Container getContainer() {
        if (instance == null) {
            // 创建一个ClassGraph对象
            ClassGraph classGraph = new ClassGraph();
            classGraph.enableAnnotationInfo();

            // 扫描所有的类
            ScanResult scanResult = classGraph.scan();
            // 获取所有的类
            var configClass = scanResult.getClassesWithAnnotation(MainConfiguration.class).get(0).loadClass();

            if (configClass == null) throw new MainConfigurationNotFoundException();
            instance = new EnhancedContainer(configClass);
        }
        return instance;
    }

    /**
     * 获取特定类型单例容器，自动寻找标记了@MainConfiguration注解的配置类
     * @param containerClass 容器的类型
     * @return 单例容器
     */
    public static Container getContainer(Class<? extends AbstractContainer> containerClass) {
        if (instance == null) {
            // 创建一个ClassGraph对象
            ClassGraph classGraph = new ClassGraph();
            classGraph.enableAnnotationInfo();

            // 扫描所有的类
            ScanResult scanResult = classGraph.scan();
            // 获取所有的类
            Class<?> configClass;
            try {
                configClass = scanResult.getClassesWithAnnotation(MainConfiguration.class).get(0).loadClass();
            } catch (IndexOutOfBoundsException e) {
                throw new MainConfigurationNotFoundException();
            }

            Constructor<? extends Container> con;
            try {
                con = containerClass.getDeclaredConstructor(Class.class);
            } catch (NoSuchMethodException e) {
                throw new ContainerConstructorException();
            }
            instance = ReflectUtil.createInstance(con, configClass);
        }
        return instance;
    }

}
