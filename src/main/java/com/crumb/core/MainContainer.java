package com.crumb.core;

import com.crumb.annotation.MainConfiguration;
import com.crumb.annotation.SelectContainer;
import com.crumb.exception.ContainerConstructorException;
import com.crumb.exception.MainConfigurationNotFoundException;
import com.crumb.util.ReflectUtil;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Constructor;



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


            ScanResult scanResult = classGraph.scan();

            Class<?> configClass;
            try {
                configClass = scanResult.getClassesWithAnnotation(MainConfiguration.class).get(0).loadClass();
            } catch (IndexOutOfBoundsException e) {
                throw new MainConfigurationNotFoundException();
            }

            Class<? extends AbstractContainer> containerClass;
            if (configClass.isAnnotationPresent(SelectContainer.class)) {
                containerClass = configClass.getAnnotation(SelectContainer.class).value();
            } else {
                containerClass = AutoContainer.class;
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
